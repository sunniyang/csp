package com.suomee.csp.domain.proto;

import java.util.List;
import java.util.Map;

import com.suomee.csp.lib.communication.SrvNode;
import com.suomee.csp.lib.proto.CspRsp;

public class QuerySrvNodesRsp extends CspRsp {
	private static final long serialVersionUID = 1L;
	private Map<String, List<SrvNode>> srvNodes;
	public Map<String, List<SrvNode>> getSrvNodes() {
		return srvNodes;
	}
	public void setSrvNodes(Map<String, List<SrvNode>> srvNodes) {
		this.srvNodes = srvNodes;
	}
}
