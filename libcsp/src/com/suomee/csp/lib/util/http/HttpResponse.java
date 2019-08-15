package com.suomee.csp.lib.util.http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
	private int statusCode;
	private Map<String, String> headers;
	private byte[] contentData;
	
	public HttpResponse() {
		this.statusCode = 200;
		this.headers = new HashMap<String, String>();
		this.contentData = null;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeader(String key, String value) {
		if (key == null || key.length() == 0) {
			return;
		}
		this.headers.put(key, value);
	}
	
	public void setContentData(byte[] contentData) {
		this.contentData = contentData;
	}
	public byte[] getContentInByte() {
		return this.contentData;
	}
	public String getContentInText() {
		if (this.contentData == null || this.contentData.length == 0) {
			return "";
		}
		String textContent = null;
		try {
			textContent = new String(this.contentData, "UTF-8");
		}
		catch (Exception e) {}
		if (textContent == null) {
			textContent = "";
		}
		return textContent;
	}
	public String getContentType() {
		String contentType = this.headers.get("Content-Type");
		if (contentType == null) {
			return "";
		}
		return contentType;
	}
	public int getContentLength() {
		String contentLength = this.headers.get("Content-Length");
		if (contentLength == null) {
			return 0;
		}
		return Integer.parseInt(contentLength);
	}
	
}
