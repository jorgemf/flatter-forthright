package com.livae.ff.api;

import com.google.appengine.api.utils.SystemProperty;

import java.util.concurrent.TimeUnit;

public class Settings {

	public static final String APP_NAME = SystemProperty.applicationId.get();

	public static final int DEFAULT_LIST_LIMIT = 20;

	public static final int MAX_LIST_LIMIT = 50;

	public static final int MIN_LIST_LIMIT = 10;

	public static final int GCM_NOTIFICATION_RETRIES = 5;

	public static final String APP_DEVELOPERS_EMAIL = System.getProperty("app.developers.email");

	public static final long TIME_BETWEEN_ANONYMOUS_COMMENTS = TimeUnit.DAYS.toMillis(10);

	public static class Google {

		public static final String API_KEY = System.getProperty("google.api.key");

		public static final String CLIENT_ID = System.getProperty("google.client.id");

		public static final String CLIENT_SECRET = System.getProperty("google.client.secret");

	}

}
