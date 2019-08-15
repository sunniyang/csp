package com.suomee.csp.lib.info;

import java.io.Serializable;

public class ServiceInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String fullName;
	private int protocal;
	private String ip;
	private int port;
	private int threads;
	private int queueMaxLength;
	private int queueTimeoutMilliseconds;
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
	public int getProtocal() {
		return protocal;
	}
	public void setProtocal(int protocal) {
		this.protocal = protocal;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getThreads() {
		return threads;
	}
	public void setThreads(int threads) {
		this.threads = threads;
	}
	public int getQueueMaxLength() {
		return queueMaxLength;
	}
	public void setQueueMaxLength(int queueMaxLength) {
		this.queueMaxLength = queueMaxLength;
	}
	public int getQueueTimeoutMilliseconds() {
		return queueTimeoutMilliseconds;
	}
	public void setQueueTimeoutMilliseconds(int queueTimeoutMilliseconds) {
		this.queueTimeoutMilliseconds = queueTimeoutMilliseconds;
	}
	
	@Override
	public String toString() {
		return "fullName=" + this.fullName + 
				", ip=" + this.ip + 
				", port=" + this.port + 
				", threads=" + this.threads + 
				", queueMaxLength=" + this.queueMaxLength + 
				", queueTimeoutMilliseconds=" + this.queueTimeoutMilliseconds;
	}
}
