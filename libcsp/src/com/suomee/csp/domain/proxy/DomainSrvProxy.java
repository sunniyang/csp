package com.suomee.csp.domain.proxy;

import com.suomee.csp.domain.DomainNames;
import com.suomee.csp.domain.proto.GetServerInfoReq;
import com.suomee.csp.domain.proto.GetServerInfoRsp;
import com.suomee.csp.domain.proto.NodeHeartbeatReq;
import com.suomee.csp.domain.proto.NodeHeartbeatRsp;
import com.suomee.csp.domain.proto.QuerySrvCmdsReq;
import com.suomee.csp.domain.proto.QuerySrvCmdsRsp;
import com.suomee.csp.domain.proto.QuerySrvNodesReq;
import com.suomee.csp.domain.proto.QuerySrvNodesRsp;
import com.suomee.csp.domain.proto.ServerHeartbeatReq;
import com.suomee.csp.domain.proto.ServerHeartbeatRsp;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.proxy.SrvProxy;

public class DomainSrvProxy extends SrvProxy {
	@Override
	public String getSrvName() {
		return DomainNames.DOMAIN_SRV_NAME;
	}
	
	public SrvFuture<GetServerInfoRsp> getServerInfo(GetServerInfoReq req) {
		return this.invoke("getServerInfo", req);
	}
	
	public SrvFuture<QuerySrvNodesRsp> querySrvNodes(QuerySrvNodesReq req) {
		return this.invoke("querySrvNodes", req);
	}
	
	public SrvFuture<QuerySrvCmdsRsp> querySrvCmds(QuerySrvCmdsReq req) {
		return this.invoke("querySrvCmds", req);
	}
	
	public SrvFuture<NodeHeartbeatRsp> nodeHeartbeat(NodeHeartbeatReq req) {
		return this.invoke("nodeHeartbeat", req);
	}
	
	public SrvFuture<ServerHeartbeatRsp> serverHeartbeat(ServerHeartbeatReq req) {
		return this.invoke("serverHeartbeat", req);
	}
}
