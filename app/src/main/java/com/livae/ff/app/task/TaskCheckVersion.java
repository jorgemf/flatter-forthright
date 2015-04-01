package com.livae.ff.app.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.livae.apphunt.api.apphunt.model.Version;
import com.livae.apphunt.app.BuildConfig;
import com.livae.apphunt.app.Settings;
import com.livae.apphunt.app.api.API;
import com.livae.apphunt.app.async.NetworkAsyncTask;

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
			version = API.version();
			prefs.edit().putLong(Settings.Pref.VERSION_CHECK_TIME, currentTime).apply();
		}
		return version;
	}

}