package com.suomee.csp.lib.proto;

import java.io.Serializable;

public class CspReq implements Serializable {
	private static final long serialVersionUID = 1L;
	protected long cspId;
	public long getCspId() {
		return cspId;
	}
	void setCspId(long cspId) {
		this.cspId = cspId;
	}
}
