package com.suomee.csp.domain.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TService {
	private long svcid;
	private long svrid;
	private String name;
	private String fullName;
	private String alias;
	private int protocal;
	private int port;
	private int threads;
	private int queueMaxLength;
	private int queueTimeoutMilliseconds;
	private String remark;
	private int status;
	private long createTime;
	public long getSvcid() {
		return svcid;
	}
	public void setSvcid(long svcid) {
		this.svcid = svcid;
	}
	public long getSvrid() {
		return svrid;
	}
	public void setSvrid(long svrid) {
		this.svrid = svrid;
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
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public int getProtocal() {
		return protocal;
	}
	public void setProtocal(int protocal) {
		this.protocal = protocal;
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
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	@Override
	public String toString() {
		return "name=" + this.name + 
				", fullName=" + this.fullName + 
				", protocal=" + this.protocal + 
				", port=" + this.port + 
				", threads=" + this.threads + 
				", queueMaxLength=" + this.queueMaxLength + 
				", queueTimeoutMilliseconds=" + this.queueTimeoutMilliseconds;
	}
	
	public static final String TABLE = "t_csp_service";
	public static final String KEY = "svcid";
	public static final String FIELDS = "svrid,name,full_name,alias,protocal,port,threads,queue_max_length,queue_timeout_milliseconds,remark,status,create_time";
	public static final String FIELDS_HOLDER = "?,?,?,?,?,?,?,?,?,?,?,?";
	public static final TService build(ResultSet rets) throws SQLException {
		TService service = new TService();
		service.setSvcid(rets.getLong("svcid"));
		service.setSvrid(rets.getLong("svrid"));
		service.setName(rets.getString("name"));
		service.setFullName(rets.getString("full_name"));
		service.setAlias(rets.getString("alias"));
		service.setProtocal(rets.getInt("protocal"));
		service.setPort(rets.getInt("port"));
		service.setThreads(rets.getInt("threads"));
		service.setQueueMaxLength(rets.getInt("queue_max_length"));
		service.setQueueTimeoutMilliseconds(rets.getInt("queue_timeout_milliseconds"));
		service.setRemark(rets.getString("remark"));
		service.setStatus(rets.getInt("status"));
		service.setCreateTime(rets.getLong("create_time"));
		return service;
	}
}
