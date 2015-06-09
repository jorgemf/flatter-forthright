package com.livae.ff.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PhoneVerification {

	private static final String VERIFICATION_TOKEN = "ff.app.verification.token";

	private static final String VERIFICATION_PHONE = "ff.app.verification.phone";

	private static final String VERIFICATION_DATE_SENT = "ff.app.verification.date_sent";

	private static PhoneVerification instance;

	private Long userPhone;

	private Integer verificationToken;

	private Long date;

	private SharedPreferences prefs;

	private PhoneVerification(Context context) {
		load(context.getApplicationContext());
	}

	public static PhoneVerification instance(Context context) {
		if (instance == null) {
			instance = new PhoneVerification(context);
		}
		return instance;
	}

	private void load(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		verificationToken = prefs.getInt(VERIFICATION_TOKEN, 0);
		if (verificationToken == 0) {
			verificationToken = null;
		}
		userPhone = prefs.getLong(VERIFICATION_PHONE, 0L);
		if (userPhone == 0) {
			userPhone = null;
		}
	}

	public Long getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(Long userPhone) {
		this.userPhone = userPhone;
		if (userPhone == null) {
			prefs.edit().remove(VERIFICATION_PHONE).apply();
		} else {
			prefs.edit().putLong(VERIFICATION_PHONE, userPhone).apply();
		}
	}

	public Long getDate() {
		return date;
	}

	public void setDate(Long date) {
		this.date = date;
		if (date == null) {
			prefs.edit().remove(VERIFICATION_DATE_SENT).apply();
		} else {
			prefs.edit().putLong(VERIFICATION_DATE_SENT, date).apply();
		}
	}

	public Integer getVerificationToken() {
		return verificationToken;
	}

	public void setVerificationToken(Integer verificationToken) {
		this.verificationToken = verificationToken;
		prefs.edit().putInt(VERIFICATION_TOKEN, verificationToken).apply();
	}

}
