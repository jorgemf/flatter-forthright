package com.livae.ff.app.task;

import android.os.AsyncTask;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.livae.ff.api.ff.Ff;
import com.livae.ff.app.AppUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.api.API;
import com.livae.ff.app.utils.DeviceUtils;
import com.livae.ff.app.utils.SyncUtils;

import java.net.HttpURLConnection;

public class TaskWakeup extends AsyncTask<Void, Void, Void> {

	@Override
	protected Void doInBackground(Void... params) {
		try {
			final AppUser appUser = Application.appUser();
			if (appUser.getUserPhone() != null) {
				// sync user profile
				SyncUtils.syncUserProfile(Application.getContext());
				// send the wake up
				Ff.ApiEndpoint.Wakeup wakeup = API.endpoint().wakeup();
				if (appUser.getCloudMessagesId() == null ||
					appUser.getAppVersion() != BuildConfig.VERSION_CODE) {
					String deviceId = DeviceUtils.getCloudMessageId(Application.getContext());
					if (deviceId != null) {
						wakeup.setDeviceId(deviceId);
					}
					wakeup.execute();
					appUser.setCloudMessagesId(deviceId);
					appUser.setAppVersion(BuildConfig.VERSION_CODE);
				} else {
					wakeup.execute();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof GoogleJsonResponseException) {
				GoogleJsonResponseException jsonException = (GoogleJsonResponseException) e;
				if (jsonException.getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
					AppUser user = Application.appUser();
					user.setAccessToken(null);
					user.setUserPhone(null);
				}
			}
		}
		return null;
	}
}
