package com.suomee.csp.log.util;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.suomee.csp.lib.proxy.SrvProxyFactory;
import com.suomee.csp.lib.server.Server;
import com.suomee.csp.log.proxy.LogSrvProxy;

public class LogClient {
	private static LogClient instance = null;
	private static Object mutex = new Object();
	private static LogClient getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new LogClient();
				}
			}
		}
		return instance;
	}
	
	private ThreadPoolExecutor executor;
	
	private LogClient() {
		this.executor = new ThreadPoolExecutor(10, 10, 
				0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	public static void shutdown() {
		LogClient.getInstance().executor.shutdown();
	}
	
	private static class Task implements Runnable {
		private String serverName;
		private String logName;
		private String content;
		private String rollover;
		private List<String> nodes;
		private Task(String serverName, String logName, String content, String rollover, List<String> nodes) {
			this.serverName = serverName;
			this.logName = logName;
			this.content = content;
			this.rollover = rollover;
			this.nodes = nodes;
		}
		@Override
		public void run() {
			String nodesString = null;
			if (this.nodes != null && this.nodes.size() > 0) {
				for (String node : this.nodes) {
					if (nodesString == null) {
						nodesString = node;
					}
					else {
						nodesString += "," + node;
					}
				}
			}
			LogSrvProxy logSrvProxy = SrvProxyFactory.getSrvProxy(LogSrvProxy.class, nodesString);
			logSrvProxy.log(this.serverName, this.logName, this.content, this.rollover);
		}
	}
	
	public static void log(String name, String content) {
		log(name, content, null, null);
	}
	
	public static void log(String name, String content, String rollover) {
		log(name, content, rollover, null);
	}
	
	public static void log(String name, String content, List<String> nodes) {
		log(name, content, null, nodes);
	}
	
	public static void log(String name, String content, String rollover, List<String> nodes) {//TODO:这个接口要删掉，nodes在配置里配好，集中打到某几台机器
		String serverName = null;
		if (Server.getInstance() != null && Server.getInstance().getServerInfo() != null) {
			serverName = Server.getInstance().getServerInfo().getFullName();
			if (serverName != null && serverName.length() == 0) {
				serverName = null;
			}
		}
		LogClient.getInstance().executor.submit(new Task(serverName, name, content, rollover, nodes));
	}
	
	public static void csplog(String name, String content, String rollover) {
		csplog(name, content, rollover, null);
	}
	
	public static void csplog(String name, String content, String rollover, List<String> nodes) {//TODO:这个接口要删掉，nodes在配置里配好，集中打到某几台机器
		LogClient.getInstance().executor.submit(new Task("", name, content, rollover, nodes));
	}
}
