package com.suomee.csp.lib.proxy;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 客户端连接池
 * 以host:port为key，为每个key建立一个FIFO队列（如果队列满，入队列会等待。如果队列空，出队列立即返回null）
 * 所有操作均线程安全
 * @author sunniyang
 *
 */
public final class ChannelPool {
	//单例
	private static ChannelPool instance = null;
	private static Object mutex = new Object();
	public static ChannelPool getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new ChannelPool();
				}
			}
		}
		return instance;
	}
	
	//记录所有当前打开的channel
	private ConcurrentMap<String, Channel> channels;
	private Map<String, LinkedBlockingQueue<Channel>> channelQueues;
	
	private ChannelPool() {
		this.channels = new ConcurrentHashMap<String, Channel>();
		this.channelQueues = new HashMap<String, LinkedBlockingQueue<Channel>>();
	}
	
	public void collect(String id, Channel channel) {
		this.channels.put(id, channel);
	}
	
	public void release(String id) {
		this.channels.remove(id);
	}
	
	public void put(String host, int port, Channel channel) {
		String key = ChannelPool.buildKey(host, port);
		LinkedBlockingQueue<Channel> channelQueue = this.channelQueues.get(key);
		if (channelQueue == null) {
			synchronized (this.channelQueues) {
				channelQueue = this.channelQueues.get(key);
				if (channelQueue == null) {
					channelQueue = new LinkedBlockingQueue<Channel>();
					this.channelQueues.put(key, channelQueue);
				}
			}
		}
		try {
			channelQueue.put(channel);
		}
		catch (InterruptedException e) {}
	}
	
	public Channel get(String host, int port) {
		String key = ChannelPool.buildKey(host, port);
		LinkedBlockingQueue<Channel> channelQueue = this.channelQueues.get(key);
		if (channelQueue != null) {
			return channelQueue.poll();
		}
		Channel channel = null;
		synchronized (this.channelQueues) {
			channelQueue = this.channelQueues.get(key);
			if (channelQueue != null) {
				channel = channelQueue.poll();
			}
		}
		return channel;
	}
	
	public int getChannelCount() {
		return this.channels.size();
	}
	
	public static String buildKey(String host, int port) {
		return host + ":" + port;
	}
}
