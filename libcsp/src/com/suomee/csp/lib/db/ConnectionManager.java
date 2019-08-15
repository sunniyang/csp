package com.suomee.csp.lib.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ConnectionManager {
	private static ConnectionManager instance = null;
	private static final Object mutex = new Object();
	public static ConnectionManager getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new ConnectionManager();
				}
			}
		}
		return instance;
	}
	
	private Map<String, Map<String, String>> dbConfigs;
	private Map<String, ComboPooledDataSource> dataSources;
	
	private ConnectionManager() {
		this.dbConfigs = new HashMap<String, Map<String, String>>();
		this.dataSources = new HashMap<String, ComboPooledDataSource>();
	}
	
	public void init(Map<String, Map<String, String>> dbConfigs) {
		this.dbConfigs = dbConfigs;
	}
	
	public Connection obtain(String dataSourceName) throws SQLException {
		Connection connection = null;
		if (!this.dataSources.containsKey(dataSourceName)) {
			synchronized (mutex) {
				if (!this.dataSources.containsKey(dataSourceName)) {
					ComboPooledDataSource ds = null;
					Map<String, String> dbConfig = this.dbConfigs.get(dataSourceName);
					if (dbConfig != null) {
						ds = this.buildDataSource(dataSourceName, dbConfig);
					}
					this.dataSources.put(dataSourceName, ds);
					if (ds != null) {
						connection = ds.getConnection();
					}
				}
			}
		}
		else {
			ComboPooledDataSource ds = this.dataSources.get(dataSourceName);
			if (ds != null) {
				connection = ds.getConnection();
			}
		}
		return connection;
	}
	
	public void release(Connection connection) {
		if (connection == null) {
			return;
		}
		try {
			if (!connection.getAutoCommit()) {
				connection.setAutoCommit(true);
			}
		}
		catch (SQLException e) {}
		try {
			connection.close();
		}
		catch (SQLException e) {}
	}
	
	public void beginTransaction(Connection connection) throws SQLException {
		if (connection == null) {
			return;
		}
		connection.setAutoCommit(false);
	}
	
	public void commitTransaction(Connection connection) throws SQLException {
		if (connection == null) {
			return;
		}
		connection.commit();
	}
	
	public void rollbackTransaction(Connection connection) {
		if (connection == null) {
			return;
		}
		try {
			connection.rollback();
		}
		catch (SQLException e) {}
	}
	
	public void close() {
		for (ComboPooledDataSource ds : this.dataSources.values()) {
			ds.close();
		}
	}
	
	private ComboPooledDataSource buildDataSource(String dsName, Map<String, String> db) {
		String name = db.get("name");
		if (name == null || name.length() == 0) {
			return null;
		}
		String host = db.get("host");
		if (host == null || host.length() == 0) {
			return null;
		}
		String port = db.get("port");
		if (port == null || port.length() == 0) {
			return null;
		}
		String user = db.get("user");
		if (user == null || user.length() == 0) {
			return null;
		}
		String pass = db.get("pass");
		if (pass == null || pass.length() == 0) {
			return null;
		}
		try {
			ComboPooledDataSource ds = new ComboPooledDataSource(dsName);
			ds.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + name + "?useUnicode=true&amp;characterEncoding=utf8&amp;zeroDateTimeBehavior=round");
			ds.setDriverClass("com.mysql.jdbc.Driver");
			ds.setUser(user);
			ds.setPassword(pass);
			//当连接池用完时客户端调用getConnection()后等待获取新连接的时间，单位ms，超时后将抛出SQLException。Default: 0，表示无限期等待。
			ds.setCheckoutTimeout(5000);
			//每隔一段时间检查所有连接池中的空闲连接，单位m。Default: 0，表示不检查。
			ds.setIdleConnectionTestPeriod(60);
			//最大空闲时间，在这段时间内未使用则连接被丢弃，单位m。Default: 0，表示永不丢弃。
			ds.setMaxIdleTime(30);
			ds.setInitialPoolSize(5);
			ds.setMinPoolSize(5);
			ds.setMaxPoolSize(10);
			//每个建立的mysql链接，能保持的最长时间，单位m。这是自建立链接以来的绝对时间，和空闲时间不一样，即使链接一直在使用，到达期限后一样会被回收。
			//对于经常使用的链接，设置这个参数更有效，没必要一直检查是否空闲。
			ds.setMaxConnectionAge(3600 * 6);
			//所有链接可持有的最大Statment数量。
			ds.setMaxStatements(0);
			ds.setMaxStatementsPerConnection(0);
			//获取新链接失败，只重试3次
			ds.setAcquireRetryAttempts(3);
			return ds;
		}
		catch (Exception e) {
			return null;
		}
	}
}
