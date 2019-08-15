package com.suomee.csp.domain.proto;

import com.suomee.csp.lib.proto.CspRsp;

public class NodeHeartbeatRsp extends CspRsp {
	private static final long serialVersionUID = 1L;
	private String ip;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
}
