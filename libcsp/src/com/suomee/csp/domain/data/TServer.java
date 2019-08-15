package com.suomee.csp.domain.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TServer {
	private long svrid;
	private String app;
	private String name;
	private String fullName;
	private String codePath;
	private String remark;
	private int status;
	private long createTime;
	public long getSvrid() {
		return svrid;
	}
	public void setSvrid(long svrid) {
		this.svrid = svrid;
	}
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
	public String getCodePath() {
		return codePath;
	}
	public void setCodePath(String codePath) {
		this.codePath = codePath;
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
		return "app=" + this.app + ", name=" + this.name;
	}
	
	public static final String TABLE = "t_csp_server";
	public static final String KEY = "svrid";
	public static final String FIELDS = "app,name,full_name,code_path,remark,status,create_time";
	public static final String FIELDS_HOLDER = "?,?,?,?,?,?,?";
	public static final TServer build(ResultSet rets) throws SQLException {
		TServer server = new TServer();
		server.setSvrid(rets.getLong("svrid"));
		server.setApp(rets.getString("app"));
		server.setName(rets.getString("name"));
		server.setFullName(rets.getString("full_name"));
		server.setCodePath(rets.getString("code_path"));
		server.setRemark(rets.getString("remark"));
		server.setStatus(rets.getInt("status"));
		server.setCreateTime(rets.getLong("create_time"));
		return server;
	}
}
