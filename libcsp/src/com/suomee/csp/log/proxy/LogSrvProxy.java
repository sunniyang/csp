package com.suomee.csp.log.proxy;

import com.suomee.csp.lib.proxy.SrvProxy;
import com.suomee.csp.log.LogNames;

public class LogSrvProxy extends SrvProxy {
	@Override
	protected String getSrvName() {
		return LogNames.LOG_SRV_NAME;
	}
	
	public void log(String serverName, String logName, String content, String rollover) {
		this.invoke("log", serverName, logName, content, rollover);
	}
}
