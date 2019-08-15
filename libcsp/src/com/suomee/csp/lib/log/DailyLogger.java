package com.suomee.csp.lib.log;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

class DailyLogger extends Logger {
	DailyLogger(String root, String name, String level, String rollover, String pattern) {
		String logName = root + name;
		this.innerLogger = org.apache.log4j.Logger.getLogger(logName);
		this.innerLogger.removeAllAppenders();
		this.innerLogger.setAdditivity(false);
		
		Level logLevel = Level.INFO;
		if (level != null) {
			if (level.equals(LogLevel.DEBUG.getLevel())) {
				logLevel = Level.DEBUG;
			}
			else if (level.equals(LogLevel.WARN.getLevel())) {
				logLevel = Level.WARN;
			}
			else if (level.equals(LogLevel.ERROR.getLevel())) {
				logLevel = Level.ERROR;
			}
		}
		this.innerLogger.setLevel(logLevel);
		
		DailyRollingFileAppender appender = new DailyRollingFileAppender();
		appender.setFile(logName + ".log");
		if (rollover != null) {
			if (rollover.equals("H")) {
				appender.setDatePattern("'.'yyyy-MM-dd-HH");
			}
			else if (rollover.equals("m")) {
				appender.setDatePattern("'.'yyyy-MM-dd-HH-mm");
			}
		}
		PatternLayout layout = new PatternLayout();
		layout.setConversionPattern(pattern);
		appender.setLayout(layout);
		appender.activateOptions();
		this.innerLogger.addAppender(appender);
	}
}
