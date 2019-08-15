package com.suomee.csp.domain.proto;

import com.suomee.csp.lib.proto.CspRsp;

public class ServerHeartbeatRsp extends CspRsp {
	private static final long serialVersionUID = 1L;
	private String fullName;
	private String ip;
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
}
