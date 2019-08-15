package com.suomee.csp.lib.log;

import java.util.HashMap;
import java.util.Map;

/**
<logs>
	<root>/log_absolute_path/</root>
	<log>
		level=debug
		type=rolling
		maxsize=1024MB
		pattern=%d{yyyy-MM-dd HH:mm:ss}|[%p]|%m%n
	</log>
	<log>
		name=logname
		level=info
	</log>
</logs>
 * @author sunniyang
 */
public class LoggerManager {
	private static final String DEFAULT_LOG_ROOT = "../logs/";
	private static final String DEFAULT_LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss}|[%p]|%m%n";
	private static final String DEFAULT_LOG_MAXSIZE = "1024MB";
	private static final String DEFAULT_LOG_ROLLOVER = "d";
	
	private static LoggerManager instance = null;
	private static final Object mutex = new Object();
	public static LoggerManager getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new LoggerManager();
				}
			}
		}
		return instance;
	}
	
	private String root;
	private Map<String, Map<String, String>> logConfigs;
	private Map<String, Logger> loggers;
	
	private LoggerManager() {
		this.root = null;
		this.logConfigs = new HashMap<String, Map<String, String>>();
		this.loggers = new HashMap<String, Logger>();
	}
	
	public void setRoot(String root) {
		this.root = root;
	}
	
	public void init(Map<String, Map<String, String>> logConfigs) {
		this.logConfigs = logConfigs;
	}
	
	Logger getLogger() {
		return this.getLogger("_", null);
	}
	
	Logger getLogger(String name) {
		return this.getLogger(name, null);
	}
	
	Logger getLogger(String name, String rollover) {
		Logger logger = this.loggers.get(name);
		if (logger == null) {
			synchronized (mutex) {
				logger = this.loggers.get(name);
				if (logger == null) {
					Map<String, String> logConfig = this.logConfigs.get(name);
					if (rollover != null) {
						if (logConfig == null) {
							logConfig = new HashMap<String, String>();
						}
						logConfig.put("rollover", rollover);
					}
					logger = this.createLogger(name, logConfig);
					this.loggers.put(name, logger);
				}
			}
		}
		return logger;
	}
	
	private Logger createLogger(String name, Map<String, String> log) {
		if (name == null || name.length() == 0) {
			name = "_";
		}
		
		String root = null;
		String type = null;
		String level = null;
		String pattern = null;
		String maxsize = null;
		String rollover = null;
		if (log != null) {
			root = log.get("root");
			type = log.get("type");
			level = log.get("level");
			pattern = log.get("pattern");
			maxsize = log.get("maxsize");
			rollover = log.get("rollover");
		}
		if (root == null || root.length() == 0) {
			root = this.root;
			if (root == null || root.length() == 0) {
				root = DEFAULT_LOG_ROOT;
			}
			if (!root.endsWith(System.getProperty("file.separator"))) {
				root += System.getProperty("file.separator");
			}
		}
		if (name.equals("_")) {
			if (type == null || type.length() == 0) {
				type = LogType.ROLLING.getType();
			}
			if (level == null || level.length() == 0) {
				level = LogLevel.DEBUG.getLevel();
			}
		}
		else {
			if (type == null || type.length() == 0) {
				type = LogType.DAILY.getType();
			}
			if (level == null || level.length() == 0) {
				level = LogLevel.INFO.getLevel();
			}
		}
		if (pattern == null || pattern.length() == 0) {
			pattern = DEFAULT_LOG_PATTERN;
		}
		if (maxsize == null || maxsize.length() == 0) {
			maxsize = DEFAULT_LOG_MAXSIZE;
		}
		if (rollover == null || rollover.length() == 0) {
			rollover = DEFAULT_LOG_ROLLOVER;
		}
		
		if (type.equals(LogType.ROLLING.getType())) {
			return new RollingLogger(root, name, level, maxsize, pattern);
		}
		else {
			return new DailyLogger(root, name, level, rollover, pattern);
		}
	}
}
