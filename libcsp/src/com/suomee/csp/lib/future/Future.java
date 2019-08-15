package com.suomee.csp.lib.future;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.suomee.csp.lib.communication.SrvException;

/**
 * Future为csp框架内所有异步机制提供了统一的回调监听模式
 * 给Future注册FutureHandler，Future会在调用complete()方法时，通知所有FutureHandler
 * 如果Future已经完成，注册时会直接执行FutureHandler
 * Future也提供了sync()方式，用来同步方法结果，但不推荐使用
 * @author sunniyang
 *
 * @param <T> Future包含的结果
 */
public class Future<T> {
	private volatile boolean completed;
	private boolean interrupted;
	private T result;
	private SrvException exception;
	private List<FutureHandler<T>> handlers;
	private Map<String, Object> attributes;
	
	public Future() {
		this.completed = false;
		this.interrupted = false;
		this.result = null;
		this.exception = null;
		this.handlers = new LinkedList<FutureHandler<T>>();
		this.attributes = new HashMap<String, Object>();
	}
	
	public Future<T> complete() {
		synchronized (this) {
			if (this.completed) {
				return this;
			}
			this.completed = true;
		}
		for (FutureHandler<T> handler : this.handlers) {
			if (this.exception != null) {
				handler.resultException(this.exception);
			}
			else {
				try {
					handler.result(this.result);
				}
				catch (Exception e) {
					handler.resultException(new SrvException(-99, "handler.result exception.", e));
				}
			}
		}
		synchronized (this) {
			this.notifyAll();
		}
		return this;
	}
	//在同步休眠中被其他线程打断时，设置打断标记（暂时没用）
	public Future<T> interrupt() {
		this.interrupted = true;
		return this;
	}
	//调用结果
	public Future<T> setResult(T result) {
		if (!this.completed) {
			this.result = result;
		}
		return this;
	}
	//执行过程中产生的异常
	public Future<T> setException(SrvException exception) {
		if (!this.completed) {
			this.exception = exception;
		}
		return this;
	}
	
	//同步
	public Future<T> sync() {
		if (this.completed) {
			return this;
		}
		synchronized (this) {
			try {
				while (!this.completed) {
					this.wait();
				}
			}
			catch (InterruptedException e) {
				this.interrupted = true;
			}
		}
		return this;
	}
	
	//注册回调处理器
	public Future<T> ready(FutureHandler<T> handler) {
		if (handler == null) {
			return this;
		}
		handler.setFuture(this);
		boolean handlable = false;
		if (this.completed) {
			handlable = true;
		}
		else {
			synchronized (this) {
				if (!this.completed) {
					this.handlers.add(handler);
				}
				else {
					handlable = true;
				}
			}
		}
		if (handlable) {
			if (this.exception != null) {
				handler.resultException(this.exception);
			}
			else {
				handler.result(this.result);
			}
		}
		return this;
	}
	
	boolean isCompleted() {
		return this.completed;
	}
	boolean isInterrupted() {
		return this.interrupted;
	}
	public T getResult() {
		return this.result;
	}
	public SrvException getException() {
		return this.exception;
	}
	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}
	public Object getAttribute(String key) {
		return this.attributes.get(key);
	}
}
