package com.suomee.csp.lib.future;

import io.netty.channel.Channel;

public class SrvFuture<T> extends Future<T> {
	private long cspId;
	private String srvName;
	private String funName;
	private long time;
	private long reqTime;
	private long rspTime;
	private int timeoutMilliseconds;
	private volatile boolean timeout;
	private Channel channel;
	private String host;
	private int port;
	
	public SrvFuture(long cspId, String srvName, String funName) {
		super();
		this.cspId = cspId;
		this.srvName = srvName;
		this.funName = funName;
		this.time = 0L;
		this.reqTime = 0L;
		this.rspTime = 0L;
		this.timeoutMilliseconds = 0;
		this.timeout = false;
		this.channel = null;
		this.host = null;
		this.port = 0;
	}
	
	public SrvFuture<T> setTime(long time) {
		this.time = time;
		return this;
	}
	public SrvFuture<T> setReqTime(long reqTime) {
		this.reqTime = reqTime;
		return this;
	}
	public SrvFuture<T> setRspTime(long rspTime) {
		this.rspTime = rspTime;
		return this;
	}
	public SrvFuture<T> setTimeoutMilliseconds(int timeoutMilliseconds) {
		this.timeoutMilliseconds = timeoutMilliseconds;
		return this;
	}
	public SrvFuture<T> setTimeout(boolean timeout) {
		this.timeout = timeout;
		return this;
	}
	public SrvFuture<T> setChannel(Channel channel) {
		this.channel = channel;
		return this;
	}
	public SrvFuture<T> setHost(String host) {
		this.host = host;
		return this;
	}
	public SrvFuture<T> setPort(int port) {
		this.port = port;
		return this;
	}
	
	public long getCspId() {
		return this.cspId;
	}
	public String getSrvName() {
		return this.srvName;
	}
	public String getFunName() {
		return this.funName;
	}
	public long getTime() {
		return this.time;
	}
	public long getReqTime() {
		return this.reqTime;
	}
	public long getRspTime() {
		return this.rspTime;
	}
	public int getTimeoutMilliseconds() {
		return this.timeoutMilliseconds;
	}
	public boolean isTimeout() {
		return this.timeout;
	}
	public Channel getChannel() {
		return this.channel;
	}
	public String getHost() {
		return this.host;
	}
	public int getPort() {
		return this.port;
	}
	
	@Override
	public String toString() {
		return "future:[cspId=" + this.cspId + ", srvName=" + this.srvName + ", funName=" + this.funName + 
				", time=" + this.time + ", reqTime=" + this.reqTime + ", rspTime=" + this.rspTime + 
				", timeoutMilliseconds=" + this.timeoutMilliseconds + ", host=" + this.host + ", port=" + this.port + "]";
	}
}
