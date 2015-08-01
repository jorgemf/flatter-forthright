package com.livae.ff.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.livae.ff.app.settings.Chats;
import com.livae.ff.app.settings.Notifications;
import com.livae.ff.app.settings.Settings;
import com.livae.ff.common.Constants.Profile;

public class AppUser {

	private static final String APP_VERSION = "ff.app.version";

	private static final String CLOUD_MESSAGES_ID = "ff.device.cloud_messages.id";

	private static final String USER_PHONE = "ff.user.phone";

	private static final String ACCESS_TOKEN = "ff.user.access_token";

	private static final String USER_PROFILE = "ff.user.profile";

	private static final String USER_BLOCKED_FORTHRIGHT_CHATS = "ff.user.blocked_forthright_chats";

	private Integer appVersion;

	private String cloudMessagesId;

	private String accessToken;

	private Long blockedForthRightChats;

	private Long userPhone;

	private Profile profile;

	private SharedPreferences prefs;

	private Notifications notifications;

	private Chats chats;

	protected AppUser(Context context) {
		load(context.getApplicationContext());
		notifications = new Notifications(prefs);
		chats = new Chats(prefs);
	}

	private void load(Context context) {
		prefs = context.getSharedPreferences(Settings.PREFERENCES_USER_FILE, Context.MODE_PRIVATE);
		appVersion = prefs.getInt(APP_VERSION, 0);
		cloudMessagesId = prefs.getString(CLOUD_MESSAGES_ID, null);
		blockedForthRightChats = prefs.getLong(USER_BLOCKED_FORTHRIGHT_CHATS, 0L);
		if (blockedForthRightChats == 0) {
			blockedForthRightChats = null;
		}
		userPhone = prefs.getLong(USER_PHONE, 0L);
		if (userPhone == 0) {
			userPhone = null;
		}
		accessToken = prefs.getString(ACCESS_TOKEN, null);
		setProfile(prefs.getString(USER_PROFILE, null));
	}

	public Integer getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(Integer appVersion) {
		this.appVersion = appVersion;
		prefs.edit().putInt(APP_VERSION, appVersion).apply();
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
		prefs.edit().putString(ACCESS_TOKEN, accessToken).apply();
	}

	public Long getBlockedForthRightChats() {
		return blockedForthRightChats;
	}

	public void setBlockedForthRightChats(Long blockDate) {
		this.blockedForthRightChats = blockDate;
		if (blockDate == null) {
			prefs.edit().remove(USER_BLOCKED_FORTHRIGHT_CHATS).apply();
		} else {
			prefs.edit().putLong(USER_BLOCKED_FORTHRIGHT_CHATS, blockDate).apply();
		}
	}

	public Long getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(Long userPhone) {
		this.userPhone = userPhone;
		if (userPhone == null) {
			prefs.edit().remove(USER_PHONE).apply();
		} else {
			prefs.edit().putLong(USER_PHONE, userPhone).apply();
		}
	}

	public boolean isDeviceConnected() {
		return accessToken != null;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		try {
			if (profile == null) {
				this.profile = null;
			} else {
				this.profile = Profile.valueOf(profile);
			}
			prefs.edit().putString(USER_PROFILE, profile).apply();
		} catch (Exception ignore) {
		}
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
		if (profile == null) {
			prefs.edit().putString(USER_PROFILE, null).apply();
		} else {
			prefs.edit().putString(USER_PROFILE, profile.name()).apply();
		}
	}

	public String getCloudMessagesId() {
		return cloudMessagesId;
	}

	public void setCloudMessagesId(String cloudMessagesId) {
		this.cloudMessagesId = cloudMessagesId;
		prefs.edit().putString(CLOUD_MESSAGES_ID, cloudMessagesId).apply();
	}

	public Notifications getNotifications() {
		return notifications;
	}

	public Chats getChats() {
		return chats;
	}

	public String toString() {
		return "[userPhone = " + userPhone + "] " +
			   "[accessToken = " + accessToken + "] " +
			   "[cloudMessagesId = " + cloudMessagesId + "] " +
			   "[appVersion = " + appVersion + "] " +
			   "[profile = " + profile + "] " +
			   "[blockedForthRightChats = " + blockedForthRightChats + "] " + chats.toString() +
			   notifications.toString();
	}

}
