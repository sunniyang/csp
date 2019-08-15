package com.suomee.csp.lib.log;

public class Logger {
	public static final String ROLLOVER_DAILY = "d";
	public static final String ROLLOVER_HOUR = "H";
	public static final String ROLLOVER_MINUTE = "m";
	public static final String ROLLOVER_DAILY_PATTERN = "yyyy-MM-dd";
	public static final String ROLLOVER_HOUR_PATTERN = "yyyy-MM-dd-HH";
	public static final String ROLLOVER_MINUTE_PATTERN = "yyyy-MM-dd-HH-mm";
	
	public static Logger getLogger() {
		return LoggerManager.getInstance().getLogger();
	}
	
	public static Logger getLogger(String name) {
		return LoggerManager.getInstance().getLogger(name);
	}
	
	public static Logger getLogger(String name, String rollover) {
		return LoggerManager.getInstance().getLogger(name, rollover);
	}
	
	protected org.apache.log4j.Logger innerLogger;
	
	Logger() {
		this.innerLogger = null;
	}
	
	public void debug(String message) {
		if (message == null) {
			message = "null";
		}
		if (this.innerLogger == null) {
			System.out.println(message);
			return;
		}
		this.innerLogger.debug(message);
	}
	
	public void info(String message) {//TODO:应该可以配置是否开启远程日志
		if (message == null) {
			message = "null";
		}
		if (this.innerLogger == null) {
			System.out.println(message);
			return;
		}
		this.innerLogger.info(message);
	}
	
	public void warn(String message) {
		if (message == null) {
			message = "null";
		}
		if (this.innerLogger == null) {
			System.out.println(message);
			return;
		}
		this.innerLogger.warn(message);
	}
	
	public void error(String message) {
		this.error(message, null);
	}
	
	public void error(String message, Throwable e) {
		if (message == null) {
			message = "null";
		}
		if (this.innerLogger == null) {
			System.out.println(message);
			return;
		}
		if (e == null) {
			this.innerLogger.error(message);
		}
		else {
			this.innerLogger.error(message, e);
		}
	}
}
