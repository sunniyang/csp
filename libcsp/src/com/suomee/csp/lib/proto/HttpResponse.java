package com.suomee.csp.lib.proto;

import org.json.JSONObject;

public class HttpResponse {
	private long cspId;
	private String srvName;
	private String cmd;
	private long reqTime;
	private long rspTime;
	private int code;
	private String msg;
	private JSONObject content;
	public long getCspId() {
		return cspId;
	}
	public void setCspId(long cspId) {
		this.cspId = cspId;
	}
	public String getSrvName() {
		return srvName;
	}
	public void setSrvName(String srvName) {
		this.srvName = srvName;
	}
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public long getReqTime() {
		return reqTime;
	}
	public void setReqTime(long reqTime) {
		this.reqTime = reqTime;
	}
	public long getRspTime() {
		return rspTime;
	}
	public void setRspTime(long rspTime) {
		this.rspTime = rspTime;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public JSONObject getContent() {
		return content;
	}
	public void setContent(JSONObject content) {
		this.content = content;
	}
}
