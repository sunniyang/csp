package com.suomee.csp.domain.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TServiceNode {
	private long scnid;
	private long svrid;
	private long svcid;
	private long nodid;
	private int port;
	private int threads;
	private int queueMaxLength;
	private int queueTimeoutMilliseconds;
	private String remark;
	private int status;
	private long createTime;
	public long getScnid() {
		return scnid;
	}
	public void setScnid(long scnid) {
		this.scnid = scnid;
	}
	public long getSvrid() {
		return svrid;
	}
	public void setSvrid(long svrid) {
		this.svrid = svrid;
	}
	public long getSvcid() {
		return svcid;
	}
	public void setSvcid(long svcid) {
		this.svcid = svcid;
	}
	public long getNodid() {
		return nodid;
	}
	public void setNodid(long nodid) {
		this.nodid = nodid;
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
		return "svrid=" + this.svrid + 
				", svcid=" + this.svcid + 
				", nodid=" + this.nodid + 
				", port=" + this.port + 
				", threads=" + this.threads + 
				", queueMaxLength=" + this.queueMaxLength + 
				", queueTimeoutMilliseconds=" + this.queueTimeoutMilliseconds;
	}
	
	public static final String TABLE = "t_csp_service_node";
	public static final String KEY = "scnid";
	public static final String FIELDS = "svrid,svcid,nodid,port,threads,queue_max_length,queue_timeout_milliseconds,remark,status,create_time";
	public static final String FIELDS_HOLDER = "?,?,?,?,?,?,?,?,?,?";
	public static final TServiceNode build(ResultSet rets) throws SQLException {
		TServiceNode serviceNode = new TServiceNode();
		serviceNode.setScnid(rets.getLong("scnid"));
		serviceNode.setSvrid(rets.getLong("svrid"));
		serviceNode.setSvcid(rets.getLong("svcid"));
		serviceNode.setNodid(rets.getLong("nodid"));
		serviceNode.setPort(rets.getInt("port"));
		serviceNode.setThreads(rets.getInt("threads"));
		serviceNode.setQueueMaxLength(rets.getInt("queue_max_length"));
		serviceNode.setQueueTimeoutMilliseconds(rets.getInt("queue_timeout_milliseconds"));
		serviceNode.setRemark(rets.getString("remark"));
		serviceNode.setStatus(rets.getInt("status"));
		serviceNode.setCreateTime(rets.getLong("create_time"));
		return serviceNode;
	}
}
