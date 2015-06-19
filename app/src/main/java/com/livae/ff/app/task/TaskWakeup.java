package com.livae.ff.app.task;

import com.livae.ff.api.ff.Ff;
import com.livae.ff.app.AppUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.utils.DeviceUtils;

public class TaskWakeup extends NetworkAsyncTask<Void, Void> {

	@Override
	protected Void doInBackground(Void aVoid) throws Exception {
		Ff.ApiEndpoint.Wakeup wakeup = API.endpoint().wakeup();
		final AppUser appUser = Application.appUser();
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
		return null;
	}

}