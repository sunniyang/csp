package com.suomee.csp.lib.proto;

import java.io.Serializable;

public class CspRsp implements Serializable {
	private static final long serialVersionUID = 1L;
	protected long cspId;
	protected int srvResult;
	protected String srvMsg;
	public long getCspId() {
		return cspId;
	}
	public CspRsp setCspId(long cspId) {
		this.cspId = cspId;
		return this;
	}
	public int getSrvResult() {
		return srvResult;
	}
	public CspRsp setSrvResult(int srvResult) {
		this.srvResult = srvResult;
		return this;
	}
	public String getSrvMsg() {
		return srvMsg;
	}
	public void setSrvMsg(String srvMsg) {
		this.srvMsg = srvMsg;
	}
}
