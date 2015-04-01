package com.livae.ff.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppUser {

	private static final String APP_VERSION = "ff.app.version";

	private static final String CLOUD_MESSAGES_ID = "ff.device.cloud_messages.id";

	private static final String USER_ID = "ff.user.id";

	private static final String ACCESS_TOKEN = "ff.user.access_token";

	private Integer appVersion;

	private String cloudMessagesId;

	private String accessToken;

	private Long userId;

	private SharedPreferences prefs;

	protected AppUser(Context context) {
		load(context.getApplicationContext());
	}

	private void load(Context context) {
		prefs = context.getSharedPreferences(Settings.PREFERENCES_USER_FILE, Context.MODE_PRIVATE);
		appVersion = prefs.getInt(APP_VERSION, 0);
		cloudMessagesId = prefs.getString(CLOUD_MESSAGES_ID, null);
		userId = prefs.getLong(USER_ID, 0L);
		if (userId == 0) {
			userId = null;
		}
		accessToken = prefs.getString(ACCESS_TOKEN, null);
	}

	public Integer getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(Integer appVersion) {
		this.appVersion = appVersion;
		prefs.edit().putInt(APP_VERSION, appVersion).apply();
	}

	public String getCloudMessagesId() {
		return cloudMessagesId;
	}

	public void setCloudMessagesId(String cloudMessagesId) {
		this.cloudMessagesId = cloudMessagesId;
		prefs.edit().putString(CLOUD_MESSAGES_ID, cloudMessagesId).apply();
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
		prefs.edit().putString(ACCESS_TOKEN, accessToken).apply();
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
		if (userId == null) {
			prefs.edit().remove(USER_ID).apply();
		} else {
			prefs.edit().putLong(USER_ID, userId).apply();
		}
	}

	public boolean isDeviceConnected() {
		return accessToken != null;
	}

	public void clean(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().clear().apply();
		load(context);
	}

}
