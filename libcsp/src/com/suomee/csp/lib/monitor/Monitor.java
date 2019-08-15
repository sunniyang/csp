package com.suomee.csp.lib.monitor;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import sun.management.ConnectorAddressLink;

import com.sun.management.OperatingSystemMXBean;
import com.sun.tools.attach.VirtualMachine;
import com.suomee.csp.lib.log.LogBuilder;
import com.suomee.csp.lib.log.Logger;
import com.suomee.csp.lib.proxy.ChannelPool;
import com.suomee.csp.lib.proxy.SrvFutureQueue;
import com.suomee.csp.lib.server.ChannelHolder;
import com.suomee.csp.lib.server.HttpSrvExecutor;
import com.suomee.csp.lib.server.Server;
import com.suomee.csp.lib.server.SrvExecutor;
import com.suomee.csp.lib.util.DateTimeUtil;
import com.suomee.csp.lib.util.EnvUtil;
import com.suomee.csp.lib.util.ScheduledTask;
import com.suomee.csp.log.util.LogClient;
import com.suomee.csp.log.util.LogName;

/**
 * 监视器，负责定时收集各类性能信息，如客户端和服务端的异步队列长度、客户端和服务端的连接数
 * 收集后，和设置的阈值做比较，如果超限则作出告警动作，并上报本次收集的信息
 * @author sunniyang
 */
public class Monitor extends ScheduledTask {
	//流量曲线（分各个不同srv，基准线用同色虚线）
	//客户端调用耗时（分各个不同srv）+服务端处理耗时（分各个不同srv）
	//客户端队列长度（分各个不同srv）+服务端队列长度（分各个不同srv）
	//客户端连接数+服务端连接数
	//错误率（分各个不同srv）+线程数
	//CPU占用率、内存占用量
	private static Monitor instance = null;
	public static Monitor getInstance() {
		if (instance == null) {
			synchronized (Monitor.class) {
				if (instance == null) {
					instance = new Monitor();
				}
			}
		}
		return instance;
	}
	
	private VirtualMachine vm;
	private OperatingSystemMXBean systemBean;
	private MemoryMXBean memoryBean;
	private ThreadMXBean threadBean;
	private boolean jmxInited;
	
	private Monitor() {
		//每分钟采集一次
		super(0, 60000, false);
		this.setDaemon(true);
		this.vm = null;
		this.systemBean = null;
		this.memoryBean = null;
		this.threadBean = null;
		this.jmxInited = false;
	}
	
	public void init() {
		if (!this.jmxInited) {
			synchronized (this) {
				if (!this.jmxInited) {
					//初始化JMX环境
					String javaHome = System.getProperty("java.home");
					String agentLibPath = javaHome + File.separator + "lib" + File.separator + "management-agent.jar";
					if (!new File(agentLibPath).exists()) {
						agentLibPath = javaHome + File.separator + "jre" + File.separator + "lib" + File.separator + "management-agent.jar";
						if (!new File(agentLibPath).exists()) {
							agentLibPath = null;
							System.out.println("[error]Monitor: management-agent.jar not found in java path.");
						}
					}
					if (agentLibPath != null) {
						int pid = EnvUtil.getPid();
						try {
							VirtualMachine vm = VirtualMachine.attach(String.valueOf(pid));
							vm.loadAgent(agentLibPath, "com.sun.management.jmxremote");
							String jmxLocalAddress = ConnectorAddressLink.importFrom(pid);
							
							JMXConnector conn = JMXConnectorFactory.connect(new JMXServiceURL(jmxLocalAddress));
							MBeanServerConnection mbs = conn.getMBeanServerConnection();
							
							this.systemBean = ManagementFactory.newPlatformMXBeanProxy(
									mbs, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
							
							this.memoryBean = ManagementFactory.newPlatformMXBeanProxy(
									mbs, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
							
							this.threadBean = ManagementFactory.newPlatformMXBeanProxy(
									mbs, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
							
							this.vm = vm;
							
							this.jmxInited = true;
						}
						catch (Exception e) {
							e.printStackTrace();
							System.out.println("[error]Monitor: init JMX exception.");
						}
					}
				}
			}
		}
		this.start();
	}
	
	public void shutdown() {
		if (this.jmxInited) {
			try {
				this.vm.detach();
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("[error]Monitor: detach vm exception.");
			}
		}
		this.stop();
	}
	
	@Override
	protected void doExecute() {
		//服务端队列长度
		Map<String, Integer> serverQueueLength = new HashMap<String, Integer>();
		serverQueueLength.putAll(SrvExecutor.getInstance().getQueueLength());
		serverQueueLength.putAll(HttpSrvExecutor.getInstance().getQueueLength());
		//服务端连接数
		int serverConnectCount = ChannelHolder.getInstance().getChannelCount();
		//客户端队列长度
		Map<String, Integer> clientQueueLength = new HashMap<String, Integer>();
		clientQueueLength.putAll(SrvFutureQueue.getInstance().getQueueLength());
		//客户端连接数
		int clientConnectCount = ChannelPool.getInstance().getChannelCount();
		
		double cpuUsage = 0.0;
		long memUsage = 0L;
		int threadCount = 0;
		if (this.jmxInited) {//TODO:读取这三个值，考虑换其他方法
			//CPU占用率
			cpuUsage = Math.round(this.systemBean.getProcessCpuLoad() * 10000) / 100.0;
			//内存占用量
			long heapUsage = this.memoryBean.getHeapMemoryUsage().getUsed();
			long noheapUsage = this.memoryBean.getNonHeapMemoryUsage().getUsed();
			memUsage = heapUsage + noheapUsage;
			//线程数
			threadCount = this.threadBean.getThreadCount();
		}
		
		LogBuilder lb = LogBuilder.getBuilder();
		lb.append(DateTimeUtil.getNowMilliSeconds());
		lb.append(Server.getInstance().getServerInfo().getFullName());
		lb.append(Server.getInstance().getServerInfo().getIp());
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Integer> entry : serverQueueLength.entrySet()) {
			sb.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
		}
		lb.append(sb.toString());
		lb.append(serverConnectCount);
		sb.delete(0, sb.length());
		for (Map.Entry<String, Integer> entry : clientQueueLength.entrySet()) {
			sb.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
		}
		lb.append(sb.toString());
		lb.append(clientConnectCount);
		lb.append(cpuUsage);
		lb.append(memUsage);
		lb.append(threadCount);
		LogClient.csplog(LogName.CSP_PERFORM, lb.getLog(), Logger.ROLLOVER_MINUTE);
	}
}
