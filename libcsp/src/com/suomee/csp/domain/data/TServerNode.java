package com.suomee.csp.domain.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TServerNode {
	private long srnid;
	private long svrid;
	private long nodid;
	private long site;
	private String deployPath;
	private long lastHeartbeat;
	private String remark;
	private int status;
	private long createTime;
	public long getSrnid() {
		return srnid;
	}
	public void setSrnid(long srnid) {
		this.srnid = srnid;
	}
	public long getSvrid() {
		return svrid;
	}
	public void setSvrid(long svrid) {
		this.svrid = svrid;
	}
	public long getNodid() {
		return nodid;
	}
	public void setNodid(long nodid) {
		this.nodid = nodid;
	}
	public long getSite() {
		return site;
	}
	public void setSite(long site) {
		this.site = site;
	}
	public String getDeployPath() {
		return deployPath;
	}
	public void setDeployPath(String deployPath) {
		this.deployPath = deployPath;
	}
	public long getLastHeartbeat() {
		return lastHeartbeat;
	}
	public void setLastHeartbeat(long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
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
		return "svrid=" + this.svrid + ", nodid=" + this.nodid + ", site=" + this.site + ", deployPath=" + this.deployPath;
	}
	
	public static final String TABLE = "t_csp_server_node";
	public static final String KEY = "srnid";
	public static final String FIELDS = "svrid,nodid,site,deploy_path,last_heartbeat,remark,status,create_time";
	public static final String FIELDS_HOLDER = "?,?,?,?,?,?,?,?";
	public static final TServerNode build(ResultSet rets) throws SQLException {
		TServerNode serverNode = new TServerNode();
		serverNode.setSrnid(rets.getLong("srnid"));
		serverNode.setSvrid(rets.getLong("svrid"));
		serverNode.setNodid(rets.getLong("nodid"));
		serverNode.setSite(rets.getLong("site"));
		serverNode.setDeployPath(rets.getString("deploy_path"));
		serverNode.setLastHeartbeat(rets.getLong("last_heartbeat"));
		serverNode.setRemark(rets.getString("remark"));
		serverNode.setStatus(rets.getInt("status"));
		serverNode.setCreateTime(rets.getLong("create_time"));
		return serverNode;
	}
}
