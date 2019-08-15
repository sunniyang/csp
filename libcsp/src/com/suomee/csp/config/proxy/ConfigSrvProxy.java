package com.suomee.csp.config.proxy;

import com.suomee.csp.config.ConfigNames;
import com.suomee.csp.config.proto.GetConfigReq;
import com.suomee.csp.config.proto.GetConfigRsp;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.proxy.SrvProxy;

public class ConfigSrvProxy extends SrvProxy {
	@Override
	protected String getSrvName() {
		return ConfigNames.CONFIG_SRV_NAME;
	}
	
	public SrvFuture<GetConfigRsp> getConfig(GetConfigReq req) {
		return this.invoke("getConfig", req);
	}
}
