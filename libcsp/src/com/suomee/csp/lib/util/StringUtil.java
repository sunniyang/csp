package com.suomee.csp.lib.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

public class StringUtil {
	private static final Object mutex = new Object();
	private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	private static MessageDigest md5Md = null;
	private static MessageDigest sha1Md = null;
	private static Random random = new Random();
	private static Pattern phonePattern = Pattern.compile("^1[2-9][0-9]{9}$");
	private static Pattern emailPattern = Pattern.compile("^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w+){1,2})$");
	
	private static MessageDigest getDigest(String mdKey) throws Exception {
		if (mdKey != null && mdKey.equals("SHA-1")) {
			if (sha1Md == null) {
				synchronized (mutex) {
					if (sha1Md == null) {
						sha1Md = MessageDigest.getInstance("SHA-1");
					}
				}
			}
			return sha1Md;
		}
		else {
			if (md5Md == null) {
				synchronized (mutex) {
					if (md5Md == null) {
						md5Md = MessageDigest.getInstance("MD5");
					}
				}
			}
			return md5Md;
		}
	}
	
	private static String digest(String value, String mdKey) {
		if (value == null || value.length() == 0) {
			return "";
		}
		
		try {
			MessageDigest md = getDigest(mdKey);
			byte[] srcBuff = value.getBytes("UTF-8");
			md.update(srcBuff);
			byte[] desBuff = md.digest();
			
			char[] strBuff = new char[desBuff.length * 2];
			for (int i = 0, j = 0; i < desBuff.length; i++) {
				strBuff[j++] = hexDigits[desBuff[i] >>> 4 & 0xF];
				strBuff[j++] = hexDigits[desBuff[i] & 0xF];
			}
			return new String(strBuff);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String md5(String value) {
		return digest(value, "MD5");
	}
	
	public static String sha1(String value) {
		return digest(value, "SHA-1");
	}
	
	public static String guid() {
		String guid = UUID.randomUUID().toString();
		return guid.substring(0, 8) + guid.substring(9, 13) + guid.substring(14, 18) + guid.substring(19, 23) + guid.substring(24);
	}
	
	public static String getRandomString() {
		return getRandomString(6);
	}
	
	public static String getRandomString(int bits) {
		if (bits < 1 || bits > 32) {
			bits = 6;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bits; i++) {
			sb.append(Math.abs(random.nextInt()) % 10);
		}
		return sb.toString();
	}
	
	public static String urlEncode(String value) {
		if (value == null || value.length() == 0) {
			return value;
		}
		try {
			return URLEncoder.encode(value, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return value;
		}
	}
	
	public static String urlDecode(String value) {
		if (value == null || value.length() == 0) {
			return value;
		}
		try {
			return URLDecoder.decode(value, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return value;
		}
	}
	
	public static boolean checkPhone(String phone) {
		if (phone == null) {
			return false;
		}
		return phonePattern.matcher(phone).matches();
	}
	
	public static boolean checkEmail(String email) {
		if (email == null) {
			return false;
		}
		return emailPattern.matcher(email).matches();
	}
	
	public static String join(final  Iterator<?> iterator, String separator) {
		if (iterator == null || separator == null) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		while (iterator.hasNext()) {
			Object obj = iterator.next();
			if (obj == null) {
				continue;
			}
			result.append(obj.toString());
			result.append(separator);
		}
		int sepLen = separator.length();
		int resultLen = result.length();
		return resultLen > sepLen ? result.substring(0, resultLen - sepLen) : "";
	}
}
