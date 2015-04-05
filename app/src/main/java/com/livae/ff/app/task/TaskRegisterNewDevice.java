package com.livae.ff.app.task;

import android.support.v4.util.Pair;
import android.util.Log;

import com.livae.ff.api.ff.model.PhoneUser;
import com.livae.ff.app.AppUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.common.Constants;

public class TaskRegisterNewDevice extends NetworkAsyncTask<Pair<String, String>, Void> {

	@Override
	protected Void doInBackground(Pair<String, String> params) throws Exception {
		AppUser appUser = Application.appUser();
		if (appUser.isDeviceConnected()) {
			Log.d(LOG_TAG, "Device already connected");
			cancel();
		} else {
			Log.d(LOG_TAG, "Register phone: " + params.second);
			PhoneUser phoneUser = API.endpoint().register(params.first, params.second).execute();
			Log.d(LOG_TAG, "Access token: " + phoneUser.getAuthToken());
			appUser.setAccessToken(phoneUser.getAuthToken());
			// update the version code every time we save the device id in the server
			appUser.setAppVersion(BuildConfig.VERSION_CODE);
			appUser.setUserPhone(phoneUser.getPhone());
			Constants.Profile profile = null;
			try {
				profile = Constants.Profile.valueOf(phoneUser.getProfile());
			} catch (Exception ignore) {
			}
			appUser.setProfile(profile);
		}
		return null;
	}

}