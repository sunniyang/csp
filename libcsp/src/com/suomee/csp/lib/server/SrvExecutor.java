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
import com.suomee.csp.lib.proto.CspRequest;
import com.suomee.csp.lib.proto.CspResponse;
import com.suomee.csp.lib.proto.EResultCode;
import com.suomee.csp.lib.util.DateTimeUtil;

/**
 * 服务端执行器，业务线程
 * @author sunniyang
 *
 */
public final class SrvExecutor {
	private static SrvExecutor instance = null;
	private static Object mutex = new Object();
	public static SrvExecutor getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new SrvExecutor();
				}
			}
		}
		return instance;
	}
	
	private Map<String, ThreadPoolExecutor> srvExecutors;
	private Map<String, Class<? extends Service>> srvHandlers;
	private boolean inited;
	
	private SrvExecutor() {
		this.srvExecutors = new HashMap<String, ThreadPoolExecutor>();
		this.srvHandlers = new HashMap<String, Class<? extends Service>>();
		this.inited = false;
	}
	
	public void init(ServerInfo serverInfo, List<ServiceInfo> serviceInfos, Map<String, Class<? extends Service>> srvs) {
		if (!this.inited) {
			synchronized (this) {
				if (!this.inited) {
					for (ServiceInfo serviceInfo : serviceInfos) {
						if (serviceInfo.getProtocal() != EProtocal.NATIVE) {
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
	
	public SrvFuture<CspResponse> submit(CspRequest cspRequest) {
		long cspId = cspRequest.getCspId();
		String srvName = cspRequest.getSrvName();
		String funName = cspRequest.getFunName();
		SrvFuture<CspResponse> cspResponseFuture = new SrvFuture<CspResponse>(cspId, srvName, funName);
		cspResponseFuture.setTime(cspRequest.getTime());
		cspResponseFuture.setReqTime(DateTimeUtil.getNowMilliSeconds());
		cspResponseFuture.setTimeoutMilliseconds(cspRequest.getTimeouts());
		
		ThreadPoolExecutor executor = this.srvExecutors.get(srvName);
		Class<? extends Service> srvClass = this.srvHandlers.get(srvName);
		if (executor == null || srvClass == null) {
			cspResponseFuture.setException(new SrvException(EResultCode.INVALID_SRV, "invalid srvName. srvName=" + srvName));
			cspResponseFuture.complete();
			return cspResponseFuture;
		}
		try {
			Service service = srvClass.newInstance();
			service.setCspRequest(cspRequest);
			service.setCspResponseFuture(cspResponseFuture);
			executor.submit(service);
		}
		catch (Exception e) {
			cspResponseFuture.setException(new SrvException(EResultCode.SERVER_ERROR, "server error.", e));
			cspResponseFuture.complete();
			return cspResponseFuture;
		}
		return cspResponseFuture;
	}
	
	public Map<String, Integer> getQueueLength() {
		Map<String, Integer> queueLength = new HashMap<String, Integer>();
		for (Map.Entry<String, ThreadPoolExecutor> entry : this.srvExecutors.entrySet()) {
			queueLength.put(entry.getKey(), entry.getValue().getQueue().size());
		}
		return queueLength;
	}
}
