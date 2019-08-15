package com.suomee.csp.lib.info;

import java.util.List;

public class FullServerInfo {
	private ServerInfo serverInfo;
	private List<ServiceInfo> serviceInfos;
	public ServerInfo getServerInfo() {
		return serverInfo;
	}
	public void setServerInfo(ServerInfo serverInfo) {
		this.serverInfo = serverInfo;
	}
	public List<ServiceInfo> getServiceInfos() {
		return serviceInfos;
	}
	public void setServiceInfos(List<ServiceInfo> serviceInfos) {
		this.serviceInfos = serviceInfos;
	}
}
