package com.suomee.csp.domain.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TNode {
	private long nodid;
	private String ip;
	private long lastHeartbeat;
	private int liveStatus;
	private String remark;
	private int status;
	private long createTime;
	public long getNodid() {
		return nodid;
	}
	public void setNodid(long nodid) {
		this.nodid = nodid;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public long getLastHeartbeat() {
		return lastHeartbeat;
	}
	public void setLastHeartbeat(long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}
	public int getLiveStatus() {
		return liveStatus;
	}
	public void setLiveStatus(int liveStatus) {
		this.liveStatus = liveStatus;
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
		return "ip=" + this.ip;
	}
	
	public static final String TABLE = "t_csp_node";
	public static final String KEY = "nodid";
	public static final String FIELDS = "ip,last_heartbeat,live_status,remark,status,create_time";
	public static final String FIELDS_HOLDER = "?,?,?,?,?,?";
	public static final TNode build(ResultSet rets) throws SQLException {
		TNode node = new TNode();
		node.setNodid(rets.getLong("nodid"));
		node.setIp(rets.getString("ip"));
		node.setLastHeartbeat(rets.getLong("last_heartbeat"));
		node.setLiveStatus(rets.getInt("live_status"));
		node.setRemark(rets.getString("remark"));
		node.setStatus(rets.getInt("status"));
		node.setCreateTime(rets.getLong("create_time"));
		return node;
	}
}
