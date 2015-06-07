package com.livae.ff.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.livae.ff.common.Constants.Profile;

public class AppUser {

	private static final String APP_VERSION = "ff.app.version";

	private static final String CLOUD_MESSAGES_ID = "ff.device.cloud_messages.id";

	private static final String USER_PHONE = "ff.user.phone";

	private static final String ACCESS_TOKEN = "ff.user.access_token";

	private static final String USER_PROFILE = "ff.user.profile";

	private static final String USER_COUNTRY_CODE = "ff.user.country_code";

	private static final String VERIFICATION_TOKEN = "ff.app.verification_token";

	private Integer appVersion;

	private String cloudMessagesId;

	private String accessToken;

	private Long userPhone;

	private String countryCode;

	private Profile profile;

	private SharedPreferences prefs;

	private Integer verificationToken;

	protected AppUser(Context context) {
		load(context.getApplicationContext());
	}

	private void load(Context context) {
		prefs = context.getSharedPreferences(Settings.PREFERENCES_USER_FILE, Context.MODE_PRIVATE);
		appVersion = prefs.getInt(APP_VERSION, 0);
		if (prefs.contains(VERIFICATION_TOKEN)) {
			verificationToken = prefs.getInt(VERIFICATION_TOKEN, 0);
		}
		cloudMessagesId = prefs.getString(CLOUD_MESSAGES_ID, null);
		countryCode = prefs.getString(USER_COUNTRY_CODE, null);
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

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
		prefs.edit().putString(USER_COUNTRY_CODE, countryCode).apply();
	}

	public boolean isDeviceConnected() {
		return accessToken != null;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
		if (profile == null) {
			prefs.edit().putString(USER_PROFILE, null).apply();
		} else {
			prefs.edit().putString(USER_PROFILE, profile.name()).apply();
		}
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
	
	public Integer getVerificationToken() {
		return verificationToken;
	}

	public void setVerificationToken(Integer verificationToken) {
		this.verificationToken = verificationToken;
		prefs.edit().putInt(VERIFICATION_TOKEN, verificationToken).apply();
	}

	public void clean(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().clear().apply();
		load(context);
	}

}
