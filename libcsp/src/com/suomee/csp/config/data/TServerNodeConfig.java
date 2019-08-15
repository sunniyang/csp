package com.suomee.csp.config.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TServerNodeConfig {
	private long sngid;
	private long svrid;
	private long nodid;
	private String name;
	private String content;
	private String remark;
	private int status;
	private long createTime;
	public long getSngid() {
		return sngid;
	}
	public void setSngid(long sngid) {
		this.sngid = sngid;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
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
		return "svrid=" + this.svrid + ", nodid=" + this.nodid + ", name=" + this.name + ", content=" + this.content;
	}
	
	public static final String TABLE = "t_csp_server_node_config";
	public static final String KEY = "sngid";
	public static final String FIELDS = "svrid,nodid,name,content,remark,status,create_time";
	public static final String FIELDS_HOLDER = "?,?,?,?,?,?,?";
	public static final TServerNodeConfig build(ResultSet rets) throws SQLException {
		TServerNodeConfig serverNodeConfig = new TServerNodeConfig();
		serverNodeConfig.setSngid(rets.getLong("sngid"));
		serverNodeConfig.setSvrid(rets.getLong("svrid"));
		serverNodeConfig.setNodid(rets.getLong("nodid"));
		serverNodeConfig.setName(rets.getString("name"));
		serverNodeConfig.setContent(rets.getString("content"));
		serverNodeConfig.setRemark(rets.getString("remark"));
		serverNodeConfig.setStatus(rets.getInt("status"));
		serverNodeConfig.setCreateTime(rets.getLong("create_time"));
		return serverNodeConfig;
	}
}
