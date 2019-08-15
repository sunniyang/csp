package com.suomee.csp.lib.proto;

import java.util.Map;

import org.json.JSONObject;

public class HttpRequest {
	private long cspId;
	private String srvName;
	private String cmd;
	private long time;
	private int timeouts;
	private JSONObject content;
	private Map<String, Object> extra;
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
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getTimeouts() {
		return timeouts;
	}
	public void setTimeouts(int timeouts) {
		this.timeouts = timeouts;
	}
	public JSONObject getContent() {
		return content;
	}
	public void setContent(JSONObject content) {
		this.content = content;
	}
	public Map<String, Object> getExtra() {
		return extra;
	}
	public void setExtra(Map<String, Object> extra) {
		this.extra = extra;
	}
}
