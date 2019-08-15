package com.suomee.csp.lib.log;

public class LogType {
	public static final LogType ROLLING = new LogType("rolling");
	public static final LogType DAILY = new LogType("daily");
	
	private String type;
	
	private LogType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return this.type;
	}
}
