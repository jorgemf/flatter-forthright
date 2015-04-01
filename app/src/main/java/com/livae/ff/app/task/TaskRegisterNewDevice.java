package com.livae.ff.app.task;

import android.content.Context;
import android.util.Log;

import com.livae.apphunt.api.apphunt.model.Device;
import com.livae.apphunt.api.apphunt.model.RegisteredDevice;
import com.livae.apphunt.app.AppUser;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.BuildConfig;
import com.livae.apphunt.app.Settings;
import com.livae.apphunt.app.api.API;
import com.livae.apphunt.app.async.NetworkAsyncTask;
import com.livae.apphunt.app.utils.DeviceUtils;
import com.livae.apphunt.common.Constants;

public class TaskRegisterNewDevice extends NetworkAsyncTask<Void, Void> {

	private Context context;

	public TaskRegisterNewDevice(Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void aVoid) throws Exception {
		AppUser appUser = Application.appUser();
		if (appUser.isDeviceConnected()) {
			Log.d(LOG_TAG, "Device already connected");
			cancel();
		} else {
			Device device = new Device();
			Constants.Country country = null;
			try {
				country = Constants.Country.valueOf(DeviceUtils.getCountry(context));
			} catch (Exception ignore) {
			}
			device.setCountry(country == null ? null : country.name());
			device.setModel(DeviceUtils.getModel(context));
			device.setOsVersion(DeviceUtils.getOsVersion(context));
			device.setDeviceId(DeviceUtils.getCloudMessageId(context));
			device.setPlatform(Settings.PLATFORM.name());
			Log.d(LOG_TAG, "Register device: " + device.getDeviceId());
			RegisteredDevice registeredDevice = API.device().registerDevice(device).execute();
			Log.d(LOG_TAG, "Access token: " + registeredDevice.getAuthToken());
			appUser.setAccessToken(registeredDevice.getAuthToken());
			// update the version code every time we save the device id in the server
			appUser.setAppVersion(BuildConfig.VERSION_CODE);
			appUser.setCountry(registeredDevice.getCountry());
			appUser.setUserId(registeredDevice.getUserId());
		}
		return null;
	}

}