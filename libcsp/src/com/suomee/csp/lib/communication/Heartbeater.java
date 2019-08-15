package com.suomee.csp.lib.communication;

import com.suomee.csp.domain.proto.NodeHeartbeatReq;
import com.suomee.csp.domain.proto.ServerHeartbeatReq;
import com.suomee.csp.domain.proxy.DomainSrvProxy;
import com.suomee.csp.lib.proxy.SrvProxyFactory;
import com.suomee.csp.lib.server.Server;
import com.suomee.csp.lib.util.EnvUtil;
import com.suomee.csp.lib.util.ScheduledTask;

public class Heartbeater extends ScheduledTask {
	private static Heartbeater instance = null;
	private static Object mutex = new Object();
	public static Heartbeater getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new Heartbeater();
				}
			}
		}
		return instance;
	}
	
	public static final int REFRESH_MILLISECONDS = 60000;
	
	private Heartbeater() {
		super(0, REFRESH_MILLISECONDS, true);
		this.setDaemon(true);
	}
	
	@Override
	protected void doExecute() {
		if (Server.getInstance() == null) {
			return;
		}
		String fullName = Server.getInstance().getServerInfo().getFullName();
		String ip = EnvUtil.getLocalAddress();
		DomainSrvProxy domainSrvProxy = SrvProxyFactory.getSrvProxy(DomainSrvProxy.class);
		if (fullName.equals(com.suomee.csp.node.NodeNames.SERVER_FULL_NAME)) {
			NodeHeartbeatReq nodeHeartbeatReq = new NodeHeartbeatReq();
			nodeHeartbeatReq.setIp(ip);
			domainSrvProxy.nodeHeartbeat(nodeHeartbeatReq);
		}
		else {
			ServerHeartbeatReq serverHeartbeatReq = new ServerHeartbeatReq();
			serverHeartbeatReq.setFullName(fullName);
			serverHeartbeatReq.setIp(ip);
			domainSrvProxy.serverHeartbeat(serverHeartbeatReq);
		}
	}
}
