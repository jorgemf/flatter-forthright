package com.livae.ff.app;

import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Stack;

public class Analytics {

	private static final String LOG_TAG = "ANALYTICS";

	public static void screen(String screenName) {
		screen(Application.getGoogleTracker(), screenName);
	}

	public static void screen(Tracker t, String screenName) {
		t.setScreenName(screenName);
		t.send(new HitBuilders.AppViewBuilder().build());
		Log.i(LOG_TAG, "SCREEN  [" + screenName + "]");
	}

	public static void event(String category, String action, String label) {
		event(Application.getGoogleTracker(), category, action, label, null);
	}

	public static void event(String category, String action) {
		event(Application.getGoogleTracker(), category, action, null, null);
	}

	public static void event(String category, String action, String label, Long value) {
		event(Application.getGoogleTracker(), category, action, label, value);
	}

	public static void event(Tracker t, String category, String action, String label, Long value) {
		HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder();
		builder.setCategory(category);
		builder.setAction(action);
		if (label != null) {
			builder.setLabel(label);
		}
		if (value != null) {
			builder.setValue(value);
		}
		t.send(builder.build());
		Log.i(LOG_TAG,
			  "EVENT  [" + category + "] [" + action + "] [" + label + "] [" + value + "]");
	}

	public static void logAndReport(Throwable throwable) {
		logAndReport(throwable, false);
	}

	public static void logAndReport(Throwable throwable, boolean fatal) {
		logAndReport(Application.getGoogleTracker(), throwable, fatal);
	}

	public static void logAndReport(Tracker tracker, Throwable throwable, boolean fatal) {
		String description = getExceptionMessage(throwable);
		tracker.send(new HitBuilders.ExceptionBuilder().setDescription(description).setFatal(fatal)
													   .build());
		Log.i(LOG_TAG, "EXCEPTION  [" + description + "]");
	}

	private static String getExceptionMessage(Throwable error) {
		final int messageLength = 350;
		Stack<Throwable> causes = new Stack<>();
		Throwable cause = error;
		causes.push(cause);
		while ((cause = cause.getCause()) != null) {
			causes.push(cause);
		}
		boolean appendedTrace = false;
		String message = "";
		while (!appendedTrace && !causes.isEmpty()) {
			Throwable e = causes.pop();
			message = e.getClass().getSimpleName();
			int index = 0;
			StackTraceElement[] traces = e.getStackTrace();
			// write first trace
			while (!appendedTrace && index < traces.length) {
				if (traces[index].getClassName().contains("com.livae.ff.app")) {
					message += " " + getMessage(traces[index]);
					appendedTrace = true;
				}
				index++;
			}
			// write the rest of the traces in the stack
			while (message.length() < messageLength && index < traces.length) {
				message += " " + getMessage(traces[index]);
				index++;
			}
		}
		if (!appendedTrace) {
			StackTraceElement[] traces = error.getStackTrace();
			message += " (" + getMessage(traces[0]) + ")";
			int index = 1;
			while (message.length() < messageLength && index < traces.length) {
				message += " " + getMessage(traces[index]);
				index++;
			}
		}
		if (message.length() >= messageLength) {
			message = message.substring(0, messageLength - 1) + "…";
		}
		return message;
	}

	private static String getMessage(StackTraceElement trace) {
		return trace.getClassName().replace("com.livae.ff.app", "") + ":" +
			   trace.getMethodName();
	}

	public static class Screen {

		public static final String S = "S";

	}

	public static class Category {

		public static final String C = "C";

	}

	public static class Action {

		public static final String A = "A";

	}

	public static class Label {

		public static final String L = "L";

	}

}
