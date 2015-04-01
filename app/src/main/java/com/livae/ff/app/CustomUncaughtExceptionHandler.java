package com.livae.ff.app;

public class CustomUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static CustomUncaughtExceptionHandler instance;

	private Thread.UncaughtExceptionHandler exceptionHandler;

	public static void configure() {
		if (instance == null) {
			Thread.UncaughtExceptionHandler exceptionHandler;
			exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
			if (exceptionHandler instanceof CustomUncaughtExceptionHandler) {
				instance = (CustomUncaughtExceptionHandler) exceptionHandler;
			} else {
				instance = new CustomUncaughtExceptionHandler();
				instance.exceptionHandler = exceptionHandler;
				Thread.setDefaultUncaughtExceptionHandler(instance);
			}
		}
	}

	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		Analytics.logAndReport(throwable, true);
		exceptionHandler.uncaughtException(thread, throwable);
	}

}
