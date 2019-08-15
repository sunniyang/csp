package com.suomee.csp.domain.proto;

import java.util.Map;

import com.suomee.csp.lib.proto.CspRsp;

public class QuerySrvCmdsRsp extends CspRsp {
	private static final long serialVersionUID = 1L;
	private Map<String, String> srvCmds;
	public Map<String, String> getSrvCmds() {
		return srvCmds;
	}
	public void setSrvCmds(Map<String, String> srvCmds) {
		this.srvCmds = srvCmds;
	}
}
