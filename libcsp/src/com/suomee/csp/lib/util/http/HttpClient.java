package com.suomee.csp.lib.util.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.suomee.csp.lib.util.DateTimeUtil;

public class HttpClient {
	public static final int HTTP_CONNECT_TIMEOUT_SECONDS = 3;
	public static final String HTTP_NEW_LINE = System.getProperty("line.separator");
	
	public static HttpResponse get(HttpRequest req) throws Exception {
		if (req == null) {
			return null;
		}
		String url = req.getUrl();
		if (url == null || url.length() <= 7) {
			return null;
		}
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			return null;
		}
		URL httpUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)httpUrl.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(HTTP_CONNECT_TIMEOUT_SECONDS * 1000);
		for (Map.Entry<String, String> headerEntry : req.getHeaders().entrySet()) {
			conn.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
		}
		conn.setDoInput(true);
		
		InputStream in = conn.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			buffer.write(buf, 0, len);
		}
		
		HttpResponse rsp = new HttpResponse();
		rsp.setStatusCode(conn.getResponseCode());
		for (Map.Entry<String, List<String>> headerEntry : conn.getHeaderFields().entrySet()) {
			if (headerEntry.getValue().isEmpty()) {
				continue;
			}
			rsp.setHeader(headerEntry.getKey(), headerEntry.getValue().get(0));
		}
		rsp.setContentData(buffer.toByteArray());
		return rsp;
	}
	
	public static HttpResponse post(HttpRequest req) throws Exception {
		if (req == null) {
			return null;
		}
		String url = req.getUrl();
		if (url == null || url.length() <= 0) {
			return null;
		}
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			return null;
		}
		URL httpUrl = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)httpUrl.openConnection();
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(HTTP_CONNECT_TIMEOUT_SECONDS * 1000);
		for (Map.Entry<String, String> headerEntry : req.getHeaders().entrySet()) {
			conn.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
		}
		
		boolean isMultipart = false;
		if (req.getFileNames() != null && !req.getFileNames().isEmpty()) {
			isMultipart = true;
		}
		else {
			if (req.getParameters() != null && !req.getParameters().isEmpty() && req.getRequestData() != null) {
				isMultipart = true;
			}
			else {
				isMultipart = false;
			}
		}
		String contentType = null;
		ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
		if (!isMultipart) {
			contentType = "application/x-www-form-urlencoded; charset=UTF-8";
			StringBuilder bodyBuilder = new StringBuilder();
			if (req.getParameters() != null && !req.getParameters().isEmpty()) {
				for (Map.Entry<String, String> paramEntry : req.getParameters().entrySet()) {
					if (bodyBuilder.length() > 0) {
						bodyBuilder.append("&");
					}
					bodyBuilder.append(paramEntry.getKey()).append("=").append(paramEntry.getValue());
				}
			}
			else {
				bodyBuilder.append(req.getRequestData());
			}
			bodyStream.write(bodyBuilder.toString().getBytes("UTF-8"));
		}
		else {
			String boundary = "--csp--" + DateTimeUtil.getNowMilliSeconds();
			contentType = "multipart/form-data; boundary=" + boundary;
			if (req.getParameters() != null && !req.getParameters().isEmpty()) {
				StringBuilder bodyBuilder = new StringBuilder();
				for (Map.Entry<String, String> paramEntry : req.getParameters().entrySet()) {
					bodyBuilder.append("--").append(boundary).append(HTTP_NEW_LINE);
					bodyBuilder.append("Content-Disposition: form-data; name=\"" + paramEntry.getKey() + "\"").append(HTTP_NEW_LINE);
					bodyBuilder.append(HTTP_NEW_LINE);
					bodyBuilder.append(paramEntry.getValue()).append(HTTP_NEW_LINE);
				}
				bodyStream.write(bodyBuilder.toString().getBytes("UTF-8"));
			}
			if (req.getRequestData() != null) {
				StringBuilder bodyBuilder = new StringBuilder();
				bodyBuilder.append("--").append(boundary).append(HTTP_NEW_LINE);
				bodyBuilder.append("Content-Disposition: form-data; name=\"requestData\"").append(HTTP_NEW_LINE);
				bodyBuilder.append(HTTP_NEW_LINE);
				bodyBuilder.append(req.getRequestData()).append(HTTP_NEW_LINE);
				bodyStream.write(bodyBuilder.toString().getBytes("UTF-8"));
			}
			if (req.getFileNames() != null && !req.getFileNames().isEmpty()) {
				for (Map.Entry<String, String> fileEntry : req.getFileNames().entrySet()) {
					String formField = fileEntry.getKey();
					String fileName = fileEntry.getValue();
					byte[] fileData = req.getFileDatas().get(formField);
					StringBuilder bodyBuilder = new StringBuilder();
					bodyBuilder.append("--").append(boundary).append(HTTP_NEW_LINE);
					bodyBuilder.append("Content-Disposition: form-data; name=\"" + formField + "\"; filename=\"" + fileName + "\"; filelength=\"" + fileData.length + "\"").append(HTTP_NEW_LINE);
					String partContentType = "application/octet-stream";
					if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
						partContentType = "image/jpeg";
					}
					else if (fileName.endsWith(".png")) {
						partContentType = "image/png";
					}
					else if (fileName.endsWith(".gif")) {
						partContentType = "image/gif";
					}
					else if (fileName.endsWith(".bmp")) {
						partContentType = "image/bmp";
					}
					else if (fileName.endsWith(".txt")) {
						partContentType = "text/plain";
					}
					bodyBuilder.append("Content-Type: " + partContentType + HTTP_NEW_LINE);
					bodyBuilder.append(HTTP_NEW_LINE);
					bodyStream.write(bodyBuilder.toString().getBytes("UTF-8"));
					bodyStream.write(req.getFileDatas().get(formField));
					bodyStream.write(HTTP_NEW_LINE.getBytes("UTF-8"));
				}
			}
			bodyStream.write(new String("--" + boundary + "--" + HTTP_NEW_LINE).getBytes("UTF-8"));
		}
		conn.setRequestProperty("Content-Type", contentType);
		conn.setRequestProperty("Content-Length", String.valueOf(bodyStream.size()));
		conn.setDoOutput(true);
		conn.setDoInput(true);
		
		OutputStream out = conn.getOutputStream();
		out.write(bodyStream.toByteArray());
		out.flush();
		out.close();
		
		InputStream in = conn.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			buffer.write(buf, 0, len);
		}
		
		HttpResponse rsp = new HttpResponse();
		rsp.setStatusCode(conn.getResponseCode());
		for (Map.Entry<String, List<String>> headerEntry : conn.getHeaderFields().entrySet()) {
			if (headerEntry.getValue().isEmpty()) {
				continue;
			}
			rsp.setHeader(headerEntry.getKey(), headerEntry.getValue().get(0));
		}
		rsp.setContentData(buffer.toByteArray());
		return rsp;
	}
}
