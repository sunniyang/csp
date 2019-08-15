package com.suomee.csp.lib.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.suomee.csp.config.proto.Config;
import com.suomee.csp.config.proto.GetConfigReq;
import com.suomee.csp.config.proto.GetConfigRsp;
import com.suomee.csp.config.proxy.ConfigSrvProxy;
import com.suomee.csp.domain.proto.GetServerInfoReq;
import com.suomee.csp.domain.proto.GetServerInfoRsp;
import com.suomee.csp.domain.proxy.DomainSrvProxy;
import com.suomee.csp.lib.communication.Communicator;
import com.suomee.csp.lib.communication.Heartbeater;
import com.suomee.csp.lib.communication.SrvNode;
import com.suomee.csp.lib.communication.SrvNodeManager;
import com.suomee.csp.lib.config.Configs;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.info.EProtocal;
import com.suomee.csp.lib.info.ServerInfo;
import com.suomee.csp.lib.info.ServiceInfo;
import com.suomee.csp.lib.log.Logger;
import com.suomee.csp.lib.monitor.Monitor;
import com.suomee.csp.lib.proxy.SrvFutureQueue;
import com.suomee.csp.lib.proxy.SrvProxyFactory;
import com.suomee.csp.lib.util.EnvUtil;
import com.suomee.csp.log.util.LogClient;

/**
 * 服务启动时，日志都控制台输出。
 * 定时上报channel连接数
 * @author sunniyang
 *
 */
public abstract class Server {
	protected static Server instance = null;
	public static final Server getInstance() {
		return Server.instance;
	}
	
	public static String parseServerName(String srvName) {
		if (srvName == null) {
			return null;
		}
		int dotIndex = srvName.lastIndexOf(".");
		if (dotIndex < 0) {
			return null;
		}
		return srvName.substring(0, dotIndex);
	}
	
	protected Logger log = Logger.getLogger();
	protected String ip = "0.0.0.0";
	protected ServerInfo serverInfo = new ServerInfo();
	protected List<ServiceInfo> serviceInfos = new LinkedList<ServiceInfo>();
	protected int rpcTimeoutMilliseconds = 0;
	protected Map<String, Class<? extends Service>> srvs = new HashMap<String, Class<? extends Service>>();
	protected Map<String, Class<? extends HttpService>> httpSrvs = new HashMap<String, Class<? extends HttpService>>();
	
	public ServerInfo getServerInfo() {
		return serverInfo;
	}
	
	//由子类提供服务全名
	protected abstract String getFullName();
	//由子类提供具体的service
	protected void initSrvs() {}
	protected void initHttpSrvs() {}
	
	protected void onStart() {}
	protected void onStop() {}
	
	//绑定ip
	protected boolean bindIp() {
		//从启动参数读取ip
		this.ip = System.getProperty("csp.ip");
		if (this.ip == null || this.ip.isEmpty()) {
			this.ip = EnvUtil.getLocalAddress();
			this.log("[info]ip to bind not specified, use default local ip:" + ip + ", if need, using -Dcsp.ip=host to do it.");
		}
		else {
			this.log("[info]ip to bind specify to " + ip + ".");
		}
		return true;
	}
	
	//设置域控制器节点
	protected boolean bindDomains() {
		//读取域节点
		String cspDomains = System.getProperty("csp.domains");
		if (cspDomains == null || cspDomains.isEmpty()) {
			this.log("[error]domain node list not specified, using -Dcsp.domains=host:port,host:port to do it. server exit.");
			return false;
		}
		this.log("[info]domain node list=" + cspDomains);
		
		//解析域节点
		List<SrvNode> domainNodes = null;
		try {
			domainNodes = SrvNode.parseSrvNodes(cspDomains);
		}
		catch (Exception e) {
			e.printStackTrace();
			this.log("[error]domain node list format error, using -Dcsp.domains=host:port,host:port to do it. server exit.");
			return false;
		}
		
		//预设置主控节点
		SrvNodeManager.getInstance().initDomainNodes(domainNodes);
		this.log("[info]domain node list inited.");
		return true;
	}
	
	//从主控拉取服务配置
	protected boolean loadServerInfo() {
		DomainSrvProxy domainSrvProxy = SrvProxyFactory.getSrvProxy(DomainSrvProxy.class);
		GetServerInfoReq getServerInfoReq = new GetServerInfoReq();
		getServerInfoReq.setFullName(this.getFullName());
		getServerInfoReq.setIp(this.ip);
		SrvFuture<GetServerInfoRsp> getServerInfoRspFuture = domainSrvProxy.getServerInfo(getServerInfoReq);
		getServerInfoRspFuture.sync();
		if (getServerInfoRspFuture.getException() != null) {
			getServerInfoRspFuture.getException().printStackTrace();
			this.log("[error]get server info from domain failed, server exit.");
			return false;
		}
		GetServerInfoRsp getServerInfoRsp = getServerInfoRspFuture.getResult();
		if (getServerInfoRsp.getServerInfo() == null || getServerInfoRsp.getServiceInfos() == null) {
			this.log("[error]get server info from domain is nothing, server exit.");
			return false;
		}
		this.serverInfo = getServerInfoRsp.getServerInfo();
		this.serviceInfos = getServerInfoRsp.getServiceInfos();
		this.rpcTimeoutMilliseconds = getServerInfoRsp.getRpcTimeoutMilliseconds();
		this.log("[info]get server info from domain successful.");
		this.log("[info]server info: ");
		this.log(this.serverInfo.toString());
		this.log("[info]services info: ");
		for (ServiceInfo serviceInfo : this.serviceInfos) {
			this.log(serviceInfo.toString());
		}
		return true;
	}
	
	//初始化通信器客户端
	protected boolean initCommunicatorClient() {
		try {
			int threads = 0;
			for (ServiceInfo serviceInfo : this.serviceInfos) {
				threads += serviceInfo.getThreads();
			}
			Map<Integer, Integer> protoThreads = new HashMap<Integer, Integer>();
			protoThreads.put(EProtocal.NATIVE, threads);
			protoThreads.put(EProtocal.HTTP, threads);
			Communicator.getInstance().initClient(protoThreads, this.rpcTimeoutMilliseconds);
		}
		catch (Exception e) {
			e.printStackTrace();
			this.log("[error]init communicator client failed, server exit.");
			return false;
		}
		this.log("[info]communicator client init successful.");
		return true;
	}
	
	//拉取配置文件
	protected boolean loadConfig() {
		//先读取本地默认配置文件
		InputStream in = this.getClass().getResourceAsStream("/conf/default.xml");
		if (in != null) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int len = -1;
				while ((len = in.read(buf)) != -1) {
					out.write(buf, 0, len);
				}
				String content = new String(out.toByteArray(), "UTF-8");
				Configs.Config config = Configs.parse(content);
				if (config != null) {
					Configs.addConfig("default", config.getLists(), config.getMaps());
				}
			}
			catch (Exception e) {
				this.log("[error]read default config[default.xml] failed.");
			}
			finally {
				if (in != null) {
					try {
						in.close();
					}
					catch (Exception ex) {}
				}
			}
		}
		
		//拉取业务配置，并初始化Configs
		ConfigSrvProxy configSrvProxy = SrvProxyFactory.getSrvProxy(ConfigSrvProxy.class);
		GetConfigReq getConfigReq = new GetConfigReq();
		getConfigReq.setFullName(this.getFullName());
		getConfigReq.setIp(this.ip);
		SrvFuture<GetConfigRsp> getConfigRspFuture = configSrvProxy.getConfig(getConfigReq);
		getConfigRspFuture.sync();
		if (getConfigRspFuture.getException() != null) {
			getConfigRspFuture.getException().printStackTrace();
			this.log("[error]get configs from config server failed, server exit.");
			return false;
		}
		GetConfigRsp getConfigRsp = getConfigRspFuture.getResult();
		Map<String, Config> configs = getConfigRsp.getConfigs();
		for (Map.Entry<String, Config> entry : configs.entrySet()) {
			Config config = entry.getValue();
			Configs.addConfig(entry.getKey(), config.getLists(), config.getMaps());
		}
		this.log("[info]get configs from config server successful.");
		return true;
	}
	
	//初始化客户端节点管理器
	protected boolean initSrvNodeManager() {
		SrvNodeManager.getInstance().init();
		this.log("[info]client node manager init successful.");
		return true;
	}
	
	//初始化服务端执行器
	protected boolean initSrvExecutor() {
		SrvExecutor.getInstance().init(this.serverInfo, this.serviceInfos, this.srvs);
		HttpSrvExecutor.getInstance().init(this.serverInfo, this.serviceInfos, this.httpSrvs);
		this.log("[info]server executor init successful.");
		return true;
	}
	
	//初始化通信器服务端
	protected boolean initCommunicatorServer() {
		try {
			Communicator.getInstance().initServer(this.serverInfo, this.serviceInfos);
		}
		catch (Exception e) {
			e.printStackTrace();
			this.log("[error]init communicator server failed, server exit.");
			return false;
		}
		this.log("[info]communicator server init successful.");
		return true;
	}
	
	protected boolean createPidFile(int pid) {
		try {
			FileWriter pidFileWriter = new FileWriter("./" + Server.getInstance().getServerInfo().getName() + ".pid", false);
			pidFileWriter.write(String.valueOf(pid));
			pidFileWriter.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			this.log("[error]create pid file failed, server exit.");
			this.stop();
			this.stdOutStartFail();
			return false;
		}
		return true;
	}
	
	//停止通信器服务端
	protected void stopCommunicatorServer() {
		Communicator.getInstance().shutdownServer();
		this.log("[info]communicator server shutdown successful.");
	}
	
	//停止服务端执行器
	protected void stopSrvExecutor() {
		SrvExecutor.getInstance().shutdown();
		HttpSrvExecutor.getInstance().shutdown();
		this.log("[info]server executor shutdown successful.");
	}
	
	//停止客户端节点管理器
	protected void stopSrvNodeManager() {
		SrvNodeManager.getInstance().shutdown();
		this.log("[info]client node manager shutdown successful.");
	}
	
	//停止通信器客户端
	protected void stopCommunicatorClient() {
		Communicator.getInstance().shutdownClient();
		this.log("[info]communicator client shutdown successful.");
	}
	
	//停止客户端异步队列
	protected void stopSrvFutureQueue() {
		SrvFutureQueue.getInstance().shutdown();
		this.log("[info]client queue shutdown successful.");
	}
	
	//停止日志线程
	protected void stopLogClient() {
		LogClient.shutdown();
	}
	
	protected void deletePidFile() {
		if (Server.getInstance().getServerInfo().getName() != null) {
			File pidFile = new File("./" + Server.getInstance().getServerInfo().getName() + ".pid");
			if (pidFile.exists()) {
				pidFile.delete();
			}
		}
	}
	
	//java -classpath xxx.jar:xxx.jar -Dcsp.domains=192.168.0.10:10055,192.168.0.20:10055 -Dcsp.ip=可以指定本机ip也可忽略
	protected void start() {
		this.initSrvs();
		this.initHttpSrvs();
		
		Server.instance = this;
		
		//下面的初始化顺序很重要，不能随意调换
		if (!this.bindIp()) {
			this.stop();
			this.stdOutStartFail();
			return;
		}
		
		if (!this.bindDomains()) {
			this.stop();
			this.stdOutStartFail();
			return;
		}
		
		if (!this.loadServerInfo()) {
			this.stop();
			this.stdOutStartFail();
			return;
		}
		
		if (!this.initCommunicatorClient()) {
			this.stop();
			this.stdOutStartFail();
			return;
		}
		
		if (!this.loadConfig()) {
			this.stop();
			this.stdOutStartFail();
			return;
		}
		
		if (!this.initSrvNodeManager()) {
			this.stop();
			this.stdOutStartFail();
			return;
		}
		
		if (!this.initSrvExecutor()) {
			this.stop();
			this.stdOutStartFail();
			return;
		}
		
		//TODO:Monitor.getInstance().init();
		
		this.onStart();
		
		if (!this.initCommunicatorServer()) {
			this.stop();
			this.stdOutStartFail();
			return;
		}
		
		//TODO:Heartbeater.getInstance().start();
		
		int pid = EnvUtil.getPid();
		if (!this.createPidFile(pid)) {
			this.stop();
			this.stdOutStartFail();
			return;
		}
		
		this.stdOutStartSuccess();
		this.log("pid:" + pid);
		
		//注册kill信号处理器
		Signal.handle(new Signal("TERM"), new SignalHandler() {
			@Override
			public void handle(Signal signal) {
				Server.getInstance().stop();
			}
		});
	}
	
	protected void stop() {
		//停止顺序也很重要，是启动的反顺序
		
		Heartbeater.getInstance().stop();
		
		this.stopCommunicatorServer();
		
		this.onStop();
		
		Monitor.getInstance().shutdown();
		
		this.stopSrvExecutor();
		
		this.stopSrvNodeManager();
		
		this.stopCommunicatorClient();
		
		this.stopSrvFutureQueue();
		
		this.stopLogClient();
		
		this.deletePidFile();
		
		this.stdOutStopSuccess();
	}
	
	protected void log(String msg) {
		this.log.info(msg);
		System.out.println(msg);
	}
	
	protected void stdOutStartSuccess() {
		System.out.println("[CSP_SERVER_START_SUCCESS]");
	}
	
	protected void stdOutStartFail() {
		System.out.println("[CSP_SERVER_START_FAIL]");
	}
	
	protected void stdOutStopSuccess() {
		System.out.println("[CSP_SERVER_STOP_SUCCESS]");
	}
	
	protected void stdOutStopFail() {
		System.out.println("[CSP_SERVER_STOP_FAIL]");
	}
}
