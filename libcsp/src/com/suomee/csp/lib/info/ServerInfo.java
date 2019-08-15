package com.suomee.csp.lib.info;

import java.io.Serializable;

public class ServerInfo implements Serializable {//TODO:serverInfo里要加cspLogNodes，指定csp日志定向打到某几台日志服务器
	private static final long serialVersionUID = 1L;
	private String app;
	private String name;
	private String fullName;
	private String ip;
	private String deployPath;
	private long site;
	public String getApp() {
		return app;
	}
	public void setApp(String app) {
		this.app = app;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
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
	public String getDeployPath() {
		return deployPath;
	}
	public void setDeployPath(String deployPath) {
		this.deployPath = deployPath;
	}
	public long getSite() {
		return site;
	}
	public void setSite(long site) {
		this.site = site;
	}
	
	@Override
	public String toString() {
		return "fullname=" + this.fullName + 
				", ip=" + this.ip + 
				", site=" + this.site;
	}
}
