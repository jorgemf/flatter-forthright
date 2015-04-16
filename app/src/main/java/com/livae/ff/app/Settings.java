package com.livae.ff.app;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("ConstantConditions")
public class Settings {

	public static final String API_URL =
	  BuildConfig.DEV ? Application.getProperty(R.string.api_url_development)
					  : Application.getProperty(R.string.api_url);

	public static final String SERVER_URL =
	  BuildConfig.DEV ? Application.getProperty(R.string.server_url_development)
					  : Application.getProperty(R.string.server_url);

	public static final String PREFERENCES_USER_FILE = "ff.preferences";

	public static final String PREFERENCES_DEBUG = "debug.preferences";

	public static final String PREFERENCE_API_IP = "ff.debug.api";

	public static final long VERSION_CHECK_DELAY = TimeUnit.DAYS.toMillis(1);

	public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	public static class Google {

		public static final String SENDER_ID = Application.getProperty(R.string.google_sender_id);

//		public static final String CLIENT_ID = Application.getProperty(R.string.google_client_id);
	}

	public static class Pref {

		public static final String VERSION_CHECK_TIME = "ff.version.time_check";
	}

}