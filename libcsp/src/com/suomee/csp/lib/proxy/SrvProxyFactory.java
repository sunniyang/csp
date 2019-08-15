package com.suomee.csp.lib.proxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.suomee.csp.lib.communication.SrvNode;

public final class SrvProxyFactory {
	private static SrvProxyFactory instance = null;
	private static Object mutex = new Object();
	private static SrvProxyFactory getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new SrvProxyFactory();
				}
			}
		}
		return instance;
	}
	
	private Map<String, SrvProxy> srvProxys;
	private Map<String, HttpSrvProxy> httpSrvProxys;
	
	private SrvProxyFactory() {
		this.srvProxys = new HashMap<String, SrvProxy>();
		this.httpSrvProxys = new HashMap<String, HttpSrvProxy>();
	}
	
	public static <T extends SrvProxy> T getSrvProxy(Class<T> clazz) {
		return getSrvProxy(clazz, null, 0);
	}
	
	public static <T extends SrvProxy> T getSrvProxy(Class<T> clazz, String nodes) {
		return getSrvProxy(clazz, nodes, 0);
	}
	
	public static <T extends SrvProxy> T getSrvProxy(Class<T> clazz, int timeoutMilliseconds) {
		return getSrvProxy(clazz, null, timeoutMilliseconds);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends SrvProxy> T getSrvProxy(Class<T> clazz, String nodes, int timeoutMilliseconds) {
		SrvProxyFactory f = SrvProxyFactory.getInstance();
		String clazzName = clazz.getName();
		T t = null;
		if (!f.srvProxys.containsKey(clazzName)) {
			synchronized (f.srvProxys) {
				if (!f.srvProxys.containsKey(clazzName)) {
					try {
						t = clazz.newInstance();
						f.srvProxys.put(clazzName, t);
					}
					catch (Exception e) {}
				}
			}
		}
		else {
			t = (T)f.srvProxys.get(clazzName);
		}
		
		//设置本地节点列表
		if (nodes != null && !nodes.isEmpty()) {
			List<SrvNode> srvNodes = SrvNode.parseSrvNodesSilently(nodes);
			t.setSrvNodes(srvNodes);
		}
		else {
			t.setSrvNodes(null);
		}
		//设置超时时间
		if (timeoutMilliseconds > 0) {
			t.setTimeoutMilliseconds(timeoutMilliseconds);
		}
		else {
			t.setTimeoutMilliseconds(null);
		}
		//设置hash key
		t.setHashKey(null);
		
		return t;
	}
	
	public static <T extends SrvProxy> T getSrvProxyWithHash(Class<T> clazz, String hashKey) {
		return getSrvProxyWithHash(clazz, hashKey, null, 0);
	}
	
	public static <T extends SrvProxy> T getSrvProxyWithHash(Class<T> clazz, String hashKey, String nodes) {
		return getSrvProxyWithHash(clazz, hashKey, nodes, 0);
	}
	
	public static <T extends SrvProxy> T getSrvProxyWithHash(Class<T> clazz, String hashKey, int timeoutMilliseconds) {
		return getSrvProxyWithHash(clazz, hashKey, null, timeoutMilliseconds);
	}
	
	public static <T extends SrvProxy> T getSrvProxyWithHash(Class<T> clazz, String hashKey, String nodes, int timeoutMilliseconds) {
		T t = getSrvProxy(clazz, nodes, timeoutMilliseconds);
		t.setHashKey(hashKey);
		return t;
	}
	
	public static HttpSrvProxy getHttpSrvProxy(String srvName) {
		return getHttpSrvProxy(srvName, null, 0);
	}
	
	public static HttpSrvProxy getHttpSrvProxy(String srvName, String nodes) {
		return getHttpSrvProxy(srvName, nodes, 0);
	}
	
	public static HttpSrvProxy getHttpSrvProxy(String srvName, int timeoutMilliseconds) {
		return getHttpSrvProxy(srvName, null, timeoutMilliseconds);
	}
	
	public static HttpSrvProxy getHttpSrvProxy(String srvName, String nodes, int timeoutMilliseconds) {
		SrvProxyFactory f = SrvProxyFactory.getInstance();
		HttpSrvProxy httpSrvProxy = f.httpSrvProxys.get(srvName);
		if (httpSrvProxy == null) {
			synchronized (f.httpSrvProxys) {
				httpSrvProxy = f.httpSrvProxys.get(srvName);
				if (httpSrvProxy == null) {
					httpSrvProxy = new HttpSrvProxy(srvName);
					f.httpSrvProxys.put(srvName, httpSrvProxy);
				}
			}
		}
		
		//设置本地节点列表
		if (nodes != null && !nodes.isEmpty()) {
			List<SrvNode> srvNodes = SrvNode.parseSrvNodesSilently(nodes);
			httpSrvProxy.setSrvNodes(srvNodes);
		}
		else {
			httpSrvProxy.setSrvNodes(null);
		}
		//设置超时时间
		if (timeoutMilliseconds > 0) {
			httpSrvProxy.setTimeoutMilliseconds(timeoutMilliseconds);
		}
		else {
			httpSrvProxy.setTimeoutMilliseconds(null);
		}
		//设置hash key
		httpSrvProxy.setHashKey(null);
		
		return httpSrvProxy;
	}
	
	public static HttpSrvProxy getHttpSrvProxyWithHash(String srvName, String hashKey) {
		return getHttpSrvProxyWithHash(srvName, hashKey, null, 0);
	}
	
	public static HttpSrvProxy getHttpSrvProxyWithHash(String srvName, String hashKey, String nodes) {
		return getHttpSrvProxyWithHash(srvName, hashKey, nodes, 0);
	}
	
	public static HttpSrvProxy getHttpSrvProxyWithHash(String srvName, String hashKey, int timeoutMilliseconds) {
		return getHttpSrvProxyWithHash(srvName, hashKey, null, timeoutMilliseconds);
	}
	
	public static HttpSrvProxy getHttpSrvProxyWithHash(String srvName, String hashKey, String nodes, int timeoutMilliseconds) {
		HttpSrvProxy httpSrvProxy = getHttpSrvProxy(srvName, nodes, timeoutMilliseconds);
		httpSrvProxy.setHashKey(hashKey);
		return httpSrvProxy;
	}
}
