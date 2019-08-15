package com.suomee.csp.domain.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TDomain {
	private long dmnid;
	private String code;
	private String name;
	private int rpcTimeoutMilliseconds;
	private String remark;
	private int status;
	private long createTime;
	public long getDmnid() {
		return dmnid;
	}
	public void setDmnid(long dmnid) {
		this.dmnid = dmnid;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRpcTimeoutMilliseconds() {
		return rpcTimeoutMilliseconds;
	}
	public void setRpcTimeoutMilliseconds(int rpcTimeoutMilliseconds) {
		this.rpcTimeoutMilliseconds = rpcTimeoutMilliseconds;
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
		return "code=" + this.code + ", name=" + this.name + ", rpcTimeoutMilliseconds=" + this.rpcTimeoutMilliseconds;
	}
	
	public static final String TABLE = "t_csp_domain";
	public static final String KEY = "dmnid";
	public static final String FIELDS = "code,name,rpc_timeout_milliseconds,remark,status,create_time";
	public static final String FIELDS_HOLDER = "?,?,?,?,?,?";
	public static final TDomain build(ResultSet rets) throws SQLException {
		TDomain domain = new TDomain();
		domain.setDmnid(rets.getLong("dmnid"));
		domain.setCode(rets.getString("code"));
		domain.setName(rets.getString("name"));
		domain.setRpcTimeoutMilliseconds(rets.getInt("rpc_timeout_milliseconds"));
		domain.setRemark(rets.getString("remark"));
		domain.setStatus(rets.getInt("status"));
		domain.setCreateTime(rets.getLong("create_time"));
		return domain;
	}
}
