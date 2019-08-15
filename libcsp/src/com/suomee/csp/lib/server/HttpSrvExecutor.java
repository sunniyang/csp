package com.suomee.csp.lib.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.suomee.csp.lib.communication.SrvException;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.info.EProtocal;
import com.suomee.csp.lib.info.ServerInfo;
import com.suomee.csp.lib.info.ServiceInfo;
import com.suomee.csp.lib.proto.EResultCode;
import com.suomee.csp.lib.proto.HttpRequest;
import com.suomee.csp.lib.proto.HttpResponse;
import com.suomee.csp.lib.util.DateTimeUtil;

public final class HttpSrvExecutor {
	private static HttpSrvExecutor instance = null;
	private static Object mutex = new Object();
	public static HttpSrvExecutor getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new HttpSrvExecutor();
				}
			}
		}
		return instance;
	}
	
	private Map<String, ThreadPoolExecutor> srvExecutors;
	private Map<String, Class<? extends HttpService>> srvHandlers;
	private boolean inited;
	
	private HttpSrvExecutor() {
		this.srvExecutors = new HashMap<String, ThreadPoolExecutor>();
		this.srvHandlers = new HashMap<String, Class<? extends HttpService>>();
		this.inited = false;
	}
	
	public void init(ServerInfo serverInfo, List<ServiceInfo> serviceInfos, Map<String, Class<? extends HttpService>> srvs) {
		if (!this.inited) {
			synchronized (this) {
				if (!this.inited) {
					for (ServiceInfo serviceInfo : serviceInfos) {
						if (serviceInfo.getProtocal() != EProtocal.HTTP) {
							continue;
						}
						this.srvExecutors.put(serviceInfo.getFullName(),
								new ThreadPoolExecutor(
										serviceInfo.getThreads(), serviceInfo.getThreads(), 
										0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()));
					}
					this.srvHandlers.putAll(srvs);
				}
			}
		}
	}
	
	public void shutdown() {
		for (ThreadPoolExecutor executor : this.srvExecutors.values()) {
			executor.shutdown();
		}
	}
	
	public SrvFuture<HttpResponse> submit(HttpRequest httpRequest) {
		long cspId = httpRequest.getCspId();
		String srvName = httpRequest.getSrvName();
		String cmd = httpRequest.getCmd();
		SrvFuture<HttpResponse> httpResponseFuture = new SrvFuture<HttpResponse>(cspId, srvName, cmd);
		httpResponseFuture.setTime(httpRequest.getTime());
		httpResponseFuture.setReqTime(DateTimeUtil.getNowMilliSeconds());
		httpResponseFuture.setTimeoutMilliseconds(httpRequest.getTimeouts());
		
		ThreadPoolExecutor executor = this.srvExecutors.get(srvName);
		Class<? extends HttpService> srvClass = this.srvHandlers.get(srvName);
		if (executor == null || srvClass == null) {
			httpResponseFuture.setException(new SrvException(EResultCode.INVALID_SRV, "invalid srvName. srvName=" + srvName));
			httpResponseFuture.complete();
			return httpResponseFuture;
		}
		try {
			HttpService service = srvClass.newInstance();
			service.setHttpRequest(httpRequest);
			service.setHttpResponseFuture(httpResponseFuture);
			executor.submit(service);
		}
		catch (Exception e) {
			httpResponseFuture.setException(new SrvException(EResultCode.SERVER_ERROR, "server error.", e));
			httpResponseFuture.complete();
			return httpResponseFuture;
		}
		return httpResponseFuture;
	}
	
	public Map<String, Integer> getQueueLength() {
		Map<String, Integer> queueLength = new HashMap<String, Integer>();
		for (Map.Entry<String, ThreadPoolExecutor> entry : this.srvExecutors.entrySet()) {
			queueLength.put(entry.getKey(), entry.getValue().getQueue().size());
		}
		return queueLength;
	}
}
