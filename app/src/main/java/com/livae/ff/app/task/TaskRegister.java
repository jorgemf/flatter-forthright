package com.livae.ff.app.task;

import android.support.annotation.NonNull;
import android.util.Log;

import com.livae.ff.api.ff.Ff;
import com.livae.ff.api.ff.model.PhoneUser;
import com.livae.ff.app.AppUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.LifeCycle;
import com.livae.ff.app.utils.DeviceUtils;
import com.livae.ff.app.utils.SyncUtils;
import com.livae.ff.common.Constants;

public class TaskRegister extends NetworkAsyncTask<LifeCycle, Long, Void> {

	public TaskRegister(@NonNull LifeCycle lifeCycle) {
		super(lifeCycle);
	}

	@Override
	protected Void doInBackground(Long phoneNumber)
	  throws Exception {
		final AppUser appUser = Application.appUser();
		if (appUser.isDeviceConnected()) {
			Log.d(LOG_TAG, "Device already connected");
			cancel();
		} else {
			Log.d(LOG_TAG, "Register phone: +" + phoneNumber);
			String deviceId = DeviceUtils.getCloudMessageId(Application.getContext());
			final Ff.ApiEndpoint.Register register = API.endpoint().register(phoneNumber);
			register.setDeviceId(deviceId);
			PhoneUser phoneUser = register.execute();
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
			// sync user profile
			SyncUtils.syncUserProfile(Application.getContext());
			SyncUtils.createAccount(Application.getContext(), phoneNumber);
		}
		return null;
	}

}
