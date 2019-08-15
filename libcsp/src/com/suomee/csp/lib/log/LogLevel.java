package com.suomee.csp.lib.log;

class LogLevel {
	public static final LogLevel DEBUG = new LogLevel("debug");
	public static final LogLevel INFO = new LogLevel("info");
	public static final LogLevel WARN = new LogLevel("warn");
	public static final LogLevel ERROR = new LogLevel("error");
	
	private String level;
	
	private LogLevel(String level) {
		this.level = level;
	}
	
	public String getLevel() {
		return this.level;
	}
}
