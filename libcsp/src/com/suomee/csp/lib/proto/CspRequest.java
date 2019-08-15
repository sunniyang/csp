package com.suomee.csp.lib.proto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.suomee.csp.lib.communication.SrvException;

public class CspRequest {
	private long cspId;
	private String srvName;
	private String funName;
	private long time;
	private int timeouts;
	private byte[] data;
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
	public String getFunName() {
		return funName;
	}
	public void setFunName(String funName) {
		this.funName = funName;
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
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	/**
	 * 客户端发送请求前，将多个请求参数编码成字节数组
	 * @param parameters
	 * @throws Exception
	 */
	public void encodeToData(Object... parameters) throws SrvException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
			if (parameters != null && parameters.length > 0) {
				for (Object parameter : parameters) {
					if (parameter instanceof CspReq) {
						CspReq cspReq = (CspReq)parameter;
						cspReq.setCspId(this.cspId);
					}
					objOut.writeObject(parameter);
				}
			}
		}
		catch (Exception e) {
			throw new SrvException(EResultCode.CLIENT_ENCODE_ERROR, "client encode error.", e);
		}
		this.data = byteOut.toByteArray();
	}
	
	/**
	 * 服务端接收请求后，将字节数据解码成参数数组
	 * @return
	 * @throws Exception
	 */
	public Object[] decodeFromData(int parameterCount) throws SrvException {
		Object[] parameters = new Object[parameterCount];
		try {
			ByteArrayInputStream byteIn = new ByteArrayInputStream(this.data);
			ObjectInputStream objIn = new ObjectInputStream(byteIn);
			for (int i = 0; i < parameterCount; i++) {
				parameters[i] = objIn.readObject();
			}
		}
		catch (Exception e) {
			throw new SrvException(EResultCode.SERVER_DECODE_ERROR, "server decode error.", e);
		}
		return parameters;
	}
}
