package com.suomee.csp.config.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TServerConfig {
	private long svgid;
	private long svrid;
	private String name;
	private String content;
	private String remark;
	private int status;
	private long createTime;
	public long getSvgid() {
		return svgid;
	}
	public void setSvgid(long svgid) {
		this.svgid = svgid;
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
		return "svrid=" + this.svrid + ", name=" + this.name + ", content=" + this.content;
	}
	
	public static final String TABLE = "t_csp_server_config";
	public static final String KEY = "svgid";
	public static final String FIELDS = "svrid,name,content,remark,status,create_time";
	public static final String FIELDS_HOLDER = "?,?,?,?,?,?";
	public static final TServerConfig build(ResultSet rets) throws SQLException {
		TServerConfig serverConfig = new TServerConfig();
		serverConfig.setSvgid(rets.getLong("svgid"));
		serverConfig.setSvrid(rets.getLong("svrid"));
		serverConfig.setName(rets.getString("name"));
		serverConfig.setContent(rets.getString("content"));
		serverConfig.setRemark(rets.getString("remark"));
		serverConfig.setStatus(rets.getInt("status"));
		serverConfig.setCreateTime(rets.getLong("create_time"));
		return serverConfig;
	}
}
