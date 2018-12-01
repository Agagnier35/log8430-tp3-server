package com.log8430.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class SparkThreadFactory implements ThreadFactory {
	private static final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
	private final Thread.UncaughtExceptionHandler handler;

	public SparkThreadFactory(Thread.UncaughtExceptionHandler handler) {
		this.handler = handler;
	}

	@Override
	public Thread newThread(Runnable run) {
		Thread thread = defaultFactory.newThread(run);
		thread.setUncaughtExceptionHandler(handler);
		return thread;
	}
}
