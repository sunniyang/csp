package com.suomee.csp.lib.proto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.suomee.csp.lib.communication.SrvException;

public class CspResponse {
	private long cspId;
	private String srvName;
	private String funName;
	private long reqTime;
	private long rspTime;
	private int code;
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
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	/**
	 * 服务端返回结果前，将结果编码成字节数据
	 * @param result
	 * @throws Exception
	 */
	public void encodeToData(Object result) throws SrvException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
			if (result instanceof CspRsp) {
				CspRsp cspRsp = (CspRsp)result;
				cspRsp.setCspId(this.cspId);
			}
			objOut.writeObject(result);
		}
		catch (Exception e) {
			throw new SrvException(EResultCode.SERVER_ENCODE_ERROR, "server encode error.", e);
		}
		this.data = byteOut.toByteArray();
	}
	
	/**
	 * 客户端收到结果后，将字节数据解码成结果参数
	 * @return
	 * @throws Exception
	 */
	public Object decodeFromData() throws SrvException {
		Object result = null;
		try {
			ByteArrayInputStream byteIn = new ByteArrayInputStream(this.data);
			ObjectInputStream objIn = new ObjectInputStream(byteIn);
			result = objIn.readObject();
		}
		catch (Exception e) {
			throw new SrvException(EResultCode.CLIENT_DECODE_ERROR, "client decode error.", e);
		}
		return result;
	}
}
