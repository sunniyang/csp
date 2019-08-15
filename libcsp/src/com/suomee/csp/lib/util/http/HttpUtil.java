package com.suomee.csp.lib.util.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class HttpUtil {
	public static byte[] getImage(String url) throws Exception {
		if (url == null || url.length() <= 7) {
			return null;
		}
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			return null;
		}
		URL httpUrl = new URL(url);
		URLConnection conn = httpUrl.openConnection();
		conn.setDoInput(true);
		
		InputStream in = conn.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			buffer.write(buf, 0, len);
		}
		return buffer.toByteArray();
	}
	
	public static String getText(String url) throws Exception {
		return new String(getImage(url), "UTF-8");
	}
	
	public static String post(String url, String data) throws Exception {
		if (url == null || url.length() <= 0) {
			return null;
		}
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			return null;
		}
		URL httpUrl = new URL(url);
		URLConnection conn = httpUrl.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		
		if (data == null) {
			data = "";
		}
		OutputStream out = conn.getOutputStream();
		out.write(data.getBytes("UTF-8"));
		out.flush();
		
		InputStream in = conn.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = in.read(buf)) != -1) {
			buffer.write(buf, 0, len);
		}
		return new String(buffer.toByteArray(), "UTF-8");
	}
	
	public static String post(String url, Map<String, String> parameters) throws Exception {
		String data = null;
		if (parameters != null && parameters.size() > 0) {
			for (Map.Entry<String, String> pEntry : parameters.entrySet()) {
				if (data == null) {
					data = pEntry.getKey() + "=" + pEntry.getValue();
				}
				else {
					data += "&" + pEntry.getKey() + "=" + pEntry.getValue();
				}
			}
		}
		return post(url, data);
	}
}
