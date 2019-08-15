package com.suomee.csp.lib.future;

import com.suomee.csp.lib.communication.SrvException;

public abstract class FutureHandler<T> {
	private Future<T> future;
	void setFuture(Future<T> future) {
		this.future = future;
	}
	public Future<T> getFuture() {
		return this.future;
	}
	
	public abstract void result(T result);
	
	public abstract void resultException(SrvException e);
}
