package com.suomee.csp.lib.communication;

public class SrvException extends Exception {
	private static final long serialVersionUID = 1L;
	private int code;
	private String message;
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public SrvException(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public SrvException(int code, String message, Throwable e) {
		super(e);
		this.code = code;
		this.message = message;
	}
}
