package com.suomee.csp.lib.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.suomee.csp.lib.communication.SrvException;
import com.suomee.csp.lib.future.SrvFuture;
import com.suomee.csp.lib.proto.EResultCode;

/**
 * 客户端异步队列，在发起异步调用后，保存future
 * @author sunniyang
 *
 */
public final class SrvFutureQueue {
	private static SrvFutureQueue instance = null;
	private static Object mutex = new Object();
	public static SrvFutureQueue getInstance() {
		if (instance == null) {
			synchronized (mutex) {
				if (instance == null) {
					instance = new SrvFutureQueue();
				}
			}
		}
		return instance;
	}
	
	//在超时队列里等待的最大毫秒数
	private static final long MILLI_SECONDS_IN_TIMEOUT_QUEUE = 30000L;
	
	private Map<String, ScheduledThreadPoolExecutor> executors;
	private Map<String, ScheduledThreadPoolExecutor> timeoutExecutors;
	private Map<String, InnerQueueItem> queueItems;//key:srvName-cspId
	
	private SrvFutureQueue() {
		this.executors = new HashMap<String, ScheduledThreadPoolExecutor>();
		this.timeoutExecutors = new HashMap<String, ScheduledThreadPoolExecutor>();
		this.queueItems = new HashMap<String, InnerQueueItem>();
	}
	
	public void shutdown() {
		for (ScheduledThreadPoolExecutor executor: this.executors.values()) {
			executor.shutdown();
		}
		for (ScheduledThreadPoolExecutor timeoutExecutor: this.timeoutExecutors.values()) {
			timeoutExecutor.shutdown();
		}
	}
	
	//进异步队列，由SrvProxy负责调用
	public <T> void push(String srvName, long cspId, SrvFuture<T> future) {
		ScheduledThreadPoolExecutor executor = this.executors.get(srvName);
		if (executor == null) {
			synchronized (this.executors) {
				executor = this.executors.get(srvName);
				if (executor == null) {
					executor = new ScheduledThreadPoolExecutor(1);
					executor.setRemoveOnCancelPolicy(true);
					this.executors.put(srvName, executor);
				}
			}
		}
		ScheduledThreadPoolExecutor timeoutExecutor = this.timeoutExecutors.get(srvName);
		if (timeoutExecutor == null) {
			synchronized (this.timeoutExecutors) {
				timeoutExecutor = this.timeoutExecutors.get(srvName);
				if (timeoutExecutor == null) {
					timeoutExecutor = new ScheduledThreadPoolExecutor(1);
					timeoutExecutor.setRemoveOnCancelPolicy(true);
					this.timeoutExecutors.put(srvName, timeoutExecutor);
				}
			}
		}
		InnerQueueItem queueItem = new InnerQueueItem();
		queueItem.setFuture(future);
		ScheduledFuture<?> executorFuture = executor.schedule(new Runnable() {
			private InnerQueueItem queueItem;
			public Runnable setQueueItem(InnerQueueItem queueItem) {
				this.queueItem = queueItem;
				return this;
			}
			@Override
			public void run() {
				//客户端异步超时，设置超时标记并做回调响应
				SrvFuture<T> future = this.queueItem.getFuture();
				this.queueItem = null;
				if (!future.isTimeout()) {
					future.setTimeout(true);
					future.setException(new SrvException(EResultCode.TIMEOUT, "client timeout. future=" + this.queueItem.getFuture())).complete();
				}
			}
		}.setQueueItem(queueItem), future.getTimeoutMilliseconds(), TimeUnit.MILLISECONDS);
		queueItem.setExecutorFuture(executorFuture);
		
		ScheduledFuture<?> timeoutExecutorFuture = timeoutExecutor.schedule(new Runnable() {
			private InnerQueueItem queueItem;
			public Runnable setQueueItem(InnerQueueItem queueItem) {
				this.queueItem = queueItem;
				return this;
			}
			@Override
			public void run() {
				//超时队列超时，从整个队列移除，如果必要把异步队列中的任务取消（基本不会发生）
				SrvFuture<T> future = this.queueItem.getFuture();
				this.queueItem = null;
				SrvFutureQueue.getInstance().pull(future.getSrvName(), future.getCspId());
				if (!future.isTimeout()) {
					future.setTimeout(true);
					future.setException(new SrvException(EResultCode.TIMEOUT, "client timeout.")).complete();
				}
			}
		}.setQueueItem(queueItem), MILLI_SECONDS_IN_TIMEOUT_QUEUE, TimeUnit.MILLISECONDS);
		queueItem.setTimeoutExecutorFuture(timeoutExecutorFuture);
		
		this.queueItems.put(SrvFutureQueue.buildKey(srvName, cspId), queueItem);
	}
	
	//出异步队列，可能由SrvProxy调用（发送失败），也可能由SrvProxyIoHandler调用（异步调用回包），也可能自己调用（超时队列超时）
	public <T> SrvFuture<T> pull(String srvName, long cspId) {
		String key = SrvFutureQueue.buildKey(srvName, cspId);
		InnerQueueItem queueItem = this.queueItems.get(key);
		if (queueItem == null) {
			return null;
		}
		InnerQueueItem removedItem = null;
		synchronized (queueItem) {
			removedItem = this.queueItems.remove(key);
		}
		if (removedItem == null) {
			return null;
		}
		ScheduledFuture<?> timeoutExecutorFuture = queueItem.getTimeoutExecutorFuture();
		if (!timeoutExecutorFuture.isDone() && !timeoutExecutorFuture.isCancelled()) {
			timeoutExecutorFuture.cancel(false);
		}
		ScheduledFuture<?> executorFuture = queueItem.getExecutorFuture();
		if (executorFuture.isCancelled()) {
			return null;
		}
		executorFuture.cancel(false);
		return queueItem.getFuture();
	}
	
	public Map<String, Integer> getQueueLength() {
		Map<String, Integer> queueLength = new HashMap<String, Integer>();
		for (Map.Entry<String, ScheduledThreadPoolExecutor> entry : this.executors.entrySet()) {
			queueLength.put(entry.getKey(), entry.getValue().getQueue().size());
		}
		return queueLength;
	}
	
	public static String buildKey(String srvName, long cspId) {
		return srvName + "-" + cspId;
	}
	
	private class InnerQueueItem {
		private Object future;
		private ScheduledFuture<?> executorFuture;
		private ScheduledFuture<?> timeoutExecutorFuture;
		@SuppressWarnings("unchecked")
		public <T> SrvFuture<T> getFuture() {
			return (SrvFuture<T>)future;
		}
		public <T> void setFuture(SrvFuture<T> future) {
			this.future = future;
		}
		public ScheduledFuture<?> getExecutorFuture() {
			return executorFuture;
		}
		public void setExecutorFuture(ScheduledFuture<?> executorFuture) {
			this.executorFuture = executorFuture;
		}
		public ScheduledFuture<?> getTimeoutExecutorFuture() {
			return timeoutExecutorFuture;
		}
		public void setTimeoutExecutorFuture(ScheduledFuture<?> timeoutExecutorFuture) {
			this.timeoutExecutorFuture = timeoutExecutorFuture;
		}
	}
}
