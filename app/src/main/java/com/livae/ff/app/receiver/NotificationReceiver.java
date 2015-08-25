package com.livae.ff.app.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.service.NotificationService;

public class NotificationReceiver extends WakefulBroadcastReceiver {

	public static final String INTENT_ACTION = BuildConfig.APPLICATION_ID + ".intent.RECEIVE";

	private static final String TAG = "GCM_ENABLED";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, intent.getExtras().toString());
		ComponentName comp =
		  new ComponentName(context.getPackageName(), NotificationService.class.getName());
		startWakefulService(context, intent.setComponent(comp));
		setResultCode(Activity.RESULT_OK);
	}
}
