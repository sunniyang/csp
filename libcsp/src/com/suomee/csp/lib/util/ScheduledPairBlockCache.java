package com.suomee.csp.lib.util;

public abstract class ScheduledPairBlockCache<T> extends ScheduledTask {
	private Object[] caches;
	private int index;
	
	protected ScheduledPairBlockCache(int delay, int interval, boolean strict) {
		super(delay, interval, strict);
		this.caches = new Object[2];
		this.index = 0;
	}
	
	protected void init(T cache) {
		int i = 0;
		this.caches[i] = cache;
		this.index = i;
	}
	
	protected void update(T cache) {
		int i = (this.index + 1) % 2;
		this.caches[i] = cache;
		this.index = i;
	}
	
	@SuppressWarnings("unchecked")
	protected T get() {
		return (T)this.caches[this.index % 2];
	}
}
