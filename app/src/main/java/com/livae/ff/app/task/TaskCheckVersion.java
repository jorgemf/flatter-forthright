package com.livae.ff.app.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.livae.ff.api.ff.model.Version;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.Settings;
import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.common.Constants;

public class TaskCheckVersion extends NetworkAsyncTask<Void, Version> {

	private SharedPreferences prefs;

	public TaskCheckVersion(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	protected Version doInBackground(Void aVoid) throws Exception {
		long currentTime = System.currentTimeMillis();
		Version version = null;
		//noinspection PointlessBooleanExpression,ConstantConditions
		if (BuildConfig.DEBUG || BuildConfig.DEV ||
			currentTime < prefs.getLong(Settings.Pref.VERSION_CHECK_TIME, 0) +
						  Settings.VERSION_CHECK_DELAY) {
			version = API.endpoint().getVersion(Constants.Platform.ANDROID);
			prefs.edit().putLong(Settings.Pref.VERSION_CHECK_TIME, currentTime).apply();
		}
		return version;
	}

}