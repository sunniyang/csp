package com.suomee.csp.domain.proto;

import java.util.List;

import com.suomee.csp.lib.info.ServerInfo;
import com.suomee.csp.lib.info.ServiceInfo;
import com.suomee.csp.lib.proto.CspRsp;

public class GetServerInfoRsp extends CspRsp {
	private static final long serialVersionUID = 1L;
	private String fullName;
	private String ip;
	private ServerInfo serverInfo;
	private List<ServiceInfo> serviceInfos;
	private int rpcTimeoutMilliseconds;
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
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
	public int getRpcTimeoutMilliseconds() {
		return rpcTimeoutMilliseconds;
	}
	public void setRpcTimeoutMilliseconds(int rpcTimeoutMilliseconds) {
		this.rpcTimeoutMilliseconds = rpcTimeoutMilliseconds;
	}
}
