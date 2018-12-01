package com.log8430.util;

public class SparkThreadExceptionHandler implements Thread.UncaughtExceptionHandler {
	public Throwable t = null;

	@Override
	public void uncaughtException(Thread thread, Throwable t) {
		t.printStackTrace();
		this.t = t;
	}
}
