package com.suomee.csp.lib.security;

public class Base64 {
	private static final char[] encodeChars = new char[] {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
		'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
		'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
		'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
		'w', 'x', 'y', 'z', '0', '1', '2', '3',
		'4', '5', '6', '7', '8', '9', '+', '/'
	};
	
	private static final byte[] decodeBytes = new byte[] {
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63,
		52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
		-1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
		15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1,
		-1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
		41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1
	};
	
	public static String encode(byte[] buffer) {
		if (buffer == null || buffer.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		int len = buffer.length;
		int i = 0;
		while (i < len) {
			int b1 = buffer[i++] & 0xff;
			if (i == len) {
				sb.append(encodeChars[b1 >>> 2]);
				sb.append(encodeChars[(b1 & 0x3) << 4]);
				sb.append("==");
				break;
			}
			int b2 = buffer[i++] & 0xff;
			if (i == len) {
				sb.append(encodeChars[b1 >>> 2]);
				sb.append(encodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
				sb.append(encodeChars[(b2 & 0x0f) << 2]);
				sb.append("=");
				break;
			}
			int b3 = buffer[i++] & 0xff;
			sb.append(encodeChars[b1 >>> 2]);
			sb.append(encodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
			sb.append(encodeChars[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
			sb.append(encodeChars[b3 & 0x3f]);
		}
		return sb.toString();
	}
	
	public static byte[] decode(String content) {
		if (content == null || content.length() == 0) {
			return new byte[0];
		}
		StringBuilder sb = new StringBuilder();
		try {
			byte[] buffer = content.getBytes("US-ASCII");
			int len = buffer.length;
			int i = 0;
			while (i < len) {
				int b1 = 0;
				do {
					b1 = decodeBytes[buffer[i++]];
				}
				while (i < len && b1 == -1);
				if (b1 == -1) {
					break;
				}
				int b2 = 0;
				do {
					b2 = decodeBytes[buffer[i++]];
				}
				while (i < len && b2 == -1);
				if (b2 == -1) {
					break;
				}
				sb.append((char)((b1 << 2) | ((b2 & 0x30) >>> 4)));
				int b3 = 0;
				do {
					b3 = buffer[i++];
					if (b3 == 61) {
						return sb.toString().getBytes("iso8859-1");
					}
					b3 = decodeBytes[b3];
				}
				while (i < len && b3 == -1);
				if (b3 == -1) {
					break;
				}
				sb.append((char)(((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));
				int b4 = 0;
				do {
					b4 = buffer[i++];
					if (b4 == 61) {
						return sb.toString().getBytes("iso8859-1");
					}
					b4 = decodeBytes[b4];
				}
				while (i < len && b4 == -1);
				if (b4 == -1) {
					break;
				}
				sb.append((char)(((b3 & 0x03) << 6) | b4));
			}
			return sb.toString().getBytes("iso8859-1");
		}
		catch (Exception e) {
			return new byte[0];
		}
	}
}
