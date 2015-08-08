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
		Log.i(LOG_TAG, "EVENT  [" + category + "] [" + action + "] [" + label + "] [" + value +
					   "]");
	}

	public static void logAndReport(Throwable throwable) {
		logAndReport(throwable, false);
	}

	public static void logAndReport(Throwable throwable, boolean fatal) {
		logAndReport(Application.getGoogleTracker(), throwable, fatal);
	}

	public static void logAndReport(String message) {
		logAndReport(message, false);
	}

	public static void logAndReport(String message, boolean fatal) {
		logAndReport(Application.getGoogleTracker(), message, fatal);
	}

	public static void logAndReport(Tracker tracker, Throwable throwable, boolean fatal) {
		logAndReport(tracker, getExceptionMessage(throwable), fatal);
	}

	public static void logAndReport(Tracker tracker, String message, boolean fatal) {
		tracker.send(new HitBuilders.ExceptionBuilder().setDescription(message).setFatal(fatal)
													   .build());
		Log.i(LOG_TAG, "EXCEPTION  [" + message + "]");
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

		public static final String SETTINGS = "SETTINGS";

		public static final String ON_BOARDING = "ON_BOARDING";

		public static final String ABOUT = "ABOUT";

		public static final String CONTACTS = "CONTACTS";

		public static final String CONVERSATION_FLATTER = "CONVERSATION_FLATTER";

		public static final String CONVERSATION_FORTHRIGHT = "CONVERSATION_FORTHRIGHT";

		public static final String CONVERSATION_PRIVATE = "CONVERSATION_PRIVATE";

		public static final String CONVERSATION_SECRET = "CONVERSATION_SECRET";

		public static final String CONVERSATION_ANONYMOUS = "CONVERSATION_ANONYMOUS";

		public static final String CHATS = "CHATS";

	}

	public static class Category {

		public static final String CONTENT = "CONTENT";

		public static final String USER = "USER";

		public static final String SMS = "SMS";

		public static final String SMS_VERIFICATION_ERROR = "SMS_VERIFICATION_ERROR";

	}

	public static class Action {

		// CONTENT

		public static final String COMMENT_FLATTER = "COMMENT_FLATTER";

		public static final String COMMENT_FORTHRIGHT = "COMMENT_FORTHRIGHT";

		public static final String COMMENT_PRIVATE = "COMMENT_PRIVATE";

		public static final String COMMENT_SECRET = "COMMENT_SECRET";

		public static final String COMMENT_ANONYMOUS = "COMMENT_ANONYMOUS";

		public static final String COMMENT_VOTED_AGREE = "COMMENT_VOTED_AGREE";

		public static final String COMMENT_VOTED_DISAGREE = "COMMENT_VOTED_DISAGREE";

		public static final String COMMENT_VOTE_REMOVED = "COMMENT_VOTE_REMOVED";

		public static final String COMMENT_FLAGGED = "COMMENT_FLAGGED";

		// USER

		public static final String EDITED_PROFILE = "EDITED_PROFILE";

		public static final String REVIEW_APP = "REVIEW_APP";

		public static final String SEND_FEEDBACK = "SEND_FEEDBACK";

		public static final String VISITED_TERMS = "VISITED_TERMS";

		public static final String SHARED_APP = "SHARED_APP";

		// SMS

		public static final String SMS_VERIFICATION = "SMS_VERIFICATION";

		public static final String SMS_SENT = "SMS_SENT";

		public static final String SMS_RECEIVED = "SMS_RECEIVED";

		public static final String SMS_DELIVERED = "SMS_DELIVERED";

		public static final String SMS_NOT_DELIVERED = "SMS_NOT_DELIVERED";

		public static final String SMS_ERROR_GENERIC = "SMS_ERROR_GENERIC";

		public static final String SMS_ERROR_NO_SERVICE = "SMS_ERROR_NO_SERVICE";

		public static final String SMS_ERROR_PDU_NULL = "SMS_ERROR_PDU_NULL";

		public static final String SMS_ERROR_RADIO_OFF = "SMS_ERROR_RADIO_OFF";

	}

	public static class Label {

	}

}
