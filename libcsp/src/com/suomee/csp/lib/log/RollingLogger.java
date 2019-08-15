package com.suomee.csp.lib.log;

import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

class RollingLogger extends Logger {
	RollingLogger(String rootPath, String name, String level, String maxsize, String pattern) {
		String logName = rootPath + name;
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
		
		RollingFileAppender appender = new RollingFileAppender();
		appender.setFile(logName + ".log");
		appender.setMaxFileSize(maxsize);
		PatternLayout layout = new PatternLayout();
		layout.setConversionPattern(pattern);
		appender.setLayout(layout);
		appender.activateOptions();
		this.innerLogger.addAppender(appender);
	}
}
