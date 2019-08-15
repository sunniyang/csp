package com.suomee.csp.lib.util;

/**
 * 可调度任务基类
 * delay: 延迟多少毫秒数后开始执行，0或小于0表示立即执行
 * interval: 每隔多少毫秒循环执行一次，0或小于0表示不循环
 * strict: 是否是严格模式，严格模式下，任务循环执行的间隔时间，会严格遵循interval指定的时间。
 *         也就是说如果interval为60秒，而执行一次任务花费5秒，则本次任务结束后55秒，会启动下一次任务。
 *         注意：严格模式下，当执行一次任务所需要的时间大于等于interval指定的间隔时，会导致下一次任务立即执行。此处的权衡，交由开发者自行决策。
 * @author sunniyang
 *
 */
public abstract class ScheduledTask {//TODO: 要支持JMX，变成受管对象
	//内部执行器
	private class Runner implements Runnable {
		private final ScheduledTask task;
		private final int delay;
		private final int interval;
		private final boolean strict;
		Runner(ScheduledTask task, int delay, int interval, boolean strict) {
			this.task = task;
			this.delay = delay;
			this.interval = interval;
			this.strict = strict;
		}
		public void run() {
			if (this.delay > 0) {
				try {
					Thread.sleep(this.delay);
				}
				catch (InterruptedException e) {
					return;
				}
			}
			while (this.task.running) {
				long t1 = System.currentTimeMillis();
				this.task.execute();
				if (this.interval <= 0) {
					return;
				}
				try {
					if (this.strict) {
						long t2 = System.currentTimeMillis();
						long t = t2 - t1;
						if (this.interval > t) {
							Thread.sleep(this.interval - t);
						}
					}
					else {
						Thread.sleep(this.interval);
					}
				}
				catch (InterruptedException e) {
					return;
				}
			}
		}
	}
	
	private final Thread runnerThread;
	private volatile boolean running;
	private volatile boolean tasking;
	
	protected ScheduledTask(int delay, int interval, boolean strict) {
		this.runnerThread = new Thread(new Runner(this, delay, interval, strict));
		this.running = false;
		this.tasking = false;
	}
	
	public void setDaemon(boolean daemon) {
		this.runnerThread.setDaemon(daemon);
	}
	
	/**
	 * 启动定时执行
	 */
	public void start() {
		if (!this.running) {
			synchronized (this) {
				if (!this.running) {
					this.running = true;
					this.runnerThread.start();
				}
			}
		}
	}
	
	/**
	 * 停止定时执行
	 */
	public void stop() {
		if (this.running) {
			synchronized (this) {
				if (this.running) {
					this.running = false;
					this.runnerThread.interrupt();
				}
			}
		}
	}
	
	public void execute() {
		if (!this.tasking) {
			boolean needTask = false;
			synchronized (this) {
				if (!this.tasking) {
					this.tasking = true;
					needTask = true;
				}
			}
			if (needTask) {
				try {
					this.doExecute();
				}
				finally {
					this.tasking = false;
				}
			}
		}
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public boolean isTasking() {
		return tasking;
	}
	
	protected abstract void doExecute();
}
