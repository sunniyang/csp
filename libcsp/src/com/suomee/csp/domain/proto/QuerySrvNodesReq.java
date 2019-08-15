package com.suomee.csp.domain.proto;

import java.util.Set;

import com.suomee.csp.lib.proto.CspReq;

public class QuerySrvNodesReq extends CspReq {
	private static final long serialVersionUID = 1L;
	private Set<String> srvNames;
	public Set<String> getSrvNames() {
		return srvNames;
	}
	public void setSrvNames(Set<String> srvNames) {
		this.srvNames = srvNames;
	}
}
