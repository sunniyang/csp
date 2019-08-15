package com.suomee.csp.lib.proto;

public class EResultCode {
	public static final int OK = 0;
	public static final int UNKNOW_ERROR = -10;
	//客户端错误
	public static final int NO_ACTIVE_SERVER = -11;
	public static final int TIMEOUT = -12;
	
	public static final int CLIENT_ENCODE_ERROR = -13;
	public static final int CLIENT_DECODE_ERROR = -14;
	public static final int CLIENT_ERROR = -15;
	//服务端错误
	public static final int SERVER_ENCODE_ERROR = -23;
	public static final int SERVER_DECODE_ERROR = -24;
	public static final int SERVER_ERROR = -25;
	public static final int INVALID_SRV = -27;
	public static final int INVALID_FUN = -28;
	
	//HTTP接入错误
	public static final int BAD_REQUEST = 30;
	public static final int REQUEST_FORMAT_ERROR = -31; //请求体结构异常
	public static final int REQUEST_PARAM_PARSE_ERROR = -40;
}
