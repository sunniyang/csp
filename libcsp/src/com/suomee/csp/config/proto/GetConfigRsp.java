package com.suomee.csp.config.proto;

import java.util.Map;

import com.suomee.csp.lib.proto.CspRsp;

public class GetConfigRsp extends CspRsp {
	private static final long serialVersionUID = 1L;
	private String fullName;
	private String ip;
	private Map<String, Config> configs;
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
	public Map<String, Config> getConfigs() {
		return configs;
	}
	public void setConfigs(Map<String, Config> configs) {
		this.configs = configs;
	}
}
