package com.suomee.csp.lib.util.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
	private String url;
	private Map<String, String> headers;
	private Map<String, String> parameters;
	private String requestData;
	private Map<String, String> fileNames;
	private Map<String, byte[]> fileDatas;
	
	public HttpRequest(String url) {
		this.url = url;
		this.headers = new HashMap<String, String>();
		this.parameters = new HashMap<String, String>();
		this.requestData = null;
		this.fileNames = new HashMap<String, String>();
		this.fileDatas = new HashMap<String, byte[]>();
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
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
	public String getContentType() {
		return this.headers.get("Content-Type");
	}
	public void setContentType(String contentType) {
		this.headers.put("Content-Type", contentType);
	}
	public int getContentLength() {
		return Integer.parseInt(this.headers.get("Content-Length"));
	}
	public void setContentLength(int contentLength) {
		this.headers.put("Content-Length", String.valueOf(contentLength));
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}
	public void setParameter(String key, String value) {
		if (key == null || key.length() == 0) {
			return;
		}
		this.parameters.put(key, value);
	}
	
	public String getRequestData() {
		return requestData;
	}
	public void setRequestData(String requestData) {
		this.requestData = requestData;
	}
	public Map<String, String> getFileNames() {
		return fileNames;
	}
	public Map<String, byte[]> getFileDatas() {
		return fileDatas;
	}
	
	public void addFile(String formField, String fileName, byte[] fileData) {
		if (formField == null || formField.length() == 0) {
			return;
		}
		this.fileNames.put(formField, fileName);
		this.fileDatas.put(formField, fileData);
	}
}
