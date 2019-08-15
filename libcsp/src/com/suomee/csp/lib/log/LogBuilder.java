package com.suomee.csp.lib.log;

public class LogBuilder {
	private static final String SPLIT = "|";
	
	public static LogBuilder getBuilder() {
		return new LogBuilder();
	}
	
	private StringBuilder strBuilder;
	private boolean empty;
	
	private LogBuilder() {
		this.strBuilder = new StringBuilder();
		this.empty = true;
	}
	
	public LogBuilder append(Object logItem) {
		if (logItem == null) {
			logItem = "null";
		}
		if (this.empty) {
			this.strBuilder.append(logItem);
			this.empty = false;
		}
		else {
			this.strBuilder.append(SPLIT).append(logItem);
		}
		return this;
	}
	
	public String getLog() {
		return this.strBuilder.toString();
	}
	
	@Override
	public String toString() {
		return this.getLog();
	}
}
