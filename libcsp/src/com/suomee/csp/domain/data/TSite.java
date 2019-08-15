package com.suomee.csp.domain.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TSite {
	private long sitid;
	private String name;
	private String remark;
	private int status;
	private long createTime;
	public long getSitid() {
		return sitid;
	}
	public void setSitid(long sitid) {
		this.sitid = sitid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
		return "name=" + this.name;
	}
	
	public static final String TABLE = "t_csp_site";
	public static final String KEY = "sitid";
	public static final String FIELDS = "name,remark,status,create_time";
	public static final String FIELDS_HOLDER = "?,?,?,?";
	public static final TSite build(ResultSet rets) throws SQLException {
		TSite site = new TSite();
		site.setSitid(rets.getLong("sitid"));
		site.setName(rets.getString("name"));
		site.setRemark(rets.getString("remark"));
		site.setStatus(rets.getInt("status"));
		site.setCreateTime(rets.getLong("create_time"));
		return site;
	}
}
