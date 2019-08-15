package com.suomee.csp.lib.server;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ChannelHolder {
	//单例
	private static ChannelHolder instance = null;
	private static Object mutex = new Object();
	public static ChannelHolder getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new ChannelHolder();
				}
			}
		}
		return instance;
	}
	
	//记录所有当前打开的channel
	private ConcurrentMap<String, Channel> channels;
	
	private ChannelHolder() {
		this.channels = new ConcurrentHashMap<String, Channel>();
	}
	
	public void collect(String id, Channel channel) {
		this.channels.put(id, channel);
	}
	
	public void release(String id) {
		this.channels.remove(id);
	}
	
	public int getChannelCount() {
		return this.channels.size();
	}
}
