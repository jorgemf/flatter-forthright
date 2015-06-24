package com.livae.ff.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IntegerRes;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.sql.DBHelper;
import com.livae.ff.app.task.TaskWakeup;
import com.livae.ff.app.utils.SyncUtils;
import com.livae.ff.common.Constants;

import java.net.HttpURLConnection;
import java.util.HashMap;

public class Application extends android.app.Application {

	private static Application instance;

	private AppUser appUser;

	private Model model;

	private HashMap<TrackerName, Tracker> trackers;

	private boolean admin;

	private boolean seeAdmin;

	public static AppUser appUser() {
		return instance.appUser;
	}

	public static Context getContext() {
		return instance.getApplicationContext();
	}

	public static String getProperty(int keyResId) {
		return instance.getString(keyResId);
	}

	public static Model model() {
		return instance.model;
	}

	public static Tracker getGoogleTracker() {
		return getGoogleTracker(TrackerName.APP_TRACKER);
	}

	public static synchronized Tracker getGoogleTracker(TrackerName trackerId) {
		HashMap<TrackerName, Tracker> trackers = instance.trackers;
		if (!trackers.containsKey(trackerId)) {
			GoogleAnalytics analytics;
			analytics = GoogleAnalytics.getInstance(instance.getApplicationContext());

			//noinspection PointlessBooleanExpression,ConstantConditions
			if (BuildConfig.DEV || BuildConfig.DEBUG || instance.admin) {
				analytics.setDryRun(true);
				analytics.setAppOptOut(true);
			}
			analytics.setLocalDispatchPeriod(1000);
			Tracker tracker = analytics.newTracker(trackerId.getXml());
			tracker.enableAdvertisingIdCollection(true);
			trackers.put(trackerId, tracker);
		}
		return trackers.get(trackerId);
	}

	public static boolean isAdmin() {
		return instance.admin;
	}

	public static void setAdmin() {
		instance.admin = instance.appUser.getProfile() == Constants.Profile.ADMIN;
	}

	public static boolean getSeeAdmin() {
		return instance.seeAdmin && instance.admin;
	}

	public static void setSeeAdmin(boolean seeAdmin) {
		instance.seeAdmin = seeAdmin;
	}

	@Override
	public void onCreate() {
		// init basic stuff
		instance = this;
		super.onCreate();
		trackers = new HashMap<>();
		getGoogleTracker(); // maybe fix the problem caught exceptions
		if (!BuildConfig.DEBUG) {
			CustomUncaughtExceptionHandler.configure();
		}
		DBHelper.clearData(this);
		appUser = new AppUser(getApplicationContext());
		setAdmin();
		seeAdmin = false;
		model = new Model(this);
		//noinspection PointlessBooleanExpression,ConstantConditions
		if (BuildConfig.DEV) {
			SharedPreferences sharedPreferences;
			sharedPreferences = getSharedPreferences(Settings.PREFERENCES_DEBUG, MODE_PRIVATE);
			if (sharedPreferences.contains(Settings.PREFERENCE_API_IP)) {
				String ip = sharedPreferences.getString(Settings.PREFERENCE_API_IP, null);
				API.changeAPIUrl("http://" + ip + ":8080/_ah/api/");
			}
		}

		// wake up to the server, only to update the last accessed times
		new TaskWakeup().execute(null, new Callback<Void, Void>() {
			@Override
			public void onComplete(CustomAsyncTask<Void, Void> task, Void param, Void result) {

			}

			@Override
			public void onError(CustomAsyncTask<Void, Void> task, Void param, Exception e) {
				if (e instanceof GoogleJsonResponseException) {
					GoogleJsonResponseException jsonException = (GoogleJsonResponseException) e;
					if (jsonException.getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
						AppUser user = Application.appUser();
						user.setAccessToken(null);
						user.setUserPhone(null);
					}
				}
			}
		});
//		SyncUtils.syncContactsEveryDay();
		SyncUtils.syncContactsWhenChange();
		SyncUtils.syncContactsNow();
	}

	public enum TrackerName {
		APP_TRACKER(R.xml.analytics_google);

		private int xml;

		TrackerName(@IntegerRes int xmlResId) {
			xml = xmlResId;
		}

		public int getXml() {
			return xml;
		}
	}

}
