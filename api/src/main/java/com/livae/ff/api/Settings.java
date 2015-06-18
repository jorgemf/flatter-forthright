package com.livae.ff.api;

import com.google.appengine.api.utils.SystemProperty;

import java.util.concurrent.TimeUnit;

public class Settings {

	public static final String APP_NAME = SystemProperty.applicationId.get();

	public static final int DEFAULT_LIST_LIMIT = 30;

	public static final int MAX_LIST_LIMIT = 60;

	public static final int MIN_LIST_LIMIT = 10;

	public static final int LIST_STEP = 10; // for cache purposes

	public static final int GCM_NOTIFICATION_RETRIES = 5;

	public static final String APP_DEVELOPERS_EMAIL = System.getProperty("app.developers.email");

	public static final long MAX_COMMENT_DATE = TimeUnit.DAYS.toMillis(100);

	public static final long MAX_TIME_BLOCK_ANONYMOUS_USER = TimeUnit.DAYS.toMillis(150);

	public static final long CONVERSATION_TIME_OUT = TimeUnit.DAYS.toMillis(1);

	public static final long FLAG_FORGET_TIME = TimeUnit.DAYS.toMillis(30);

	public static final long MIN_FLAG_TO_MARK_USER = 3;

	public static final int NOTIFY_PUBLIC_COMMENTS_LAST_COMMENTERS = 20;

	public static class Google {

		public static final String API_KEY = System.getProperty("google.api.key");

		public static final String CLIENT_ID = System.getProperty("google.client.id");

		public static final String CLIENT_SECRET = System.getProperty("google.client.secret");

	}

}
