package com.livae.ff.app.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.service.CloudMessagesService;

public class NotificationReceiver extends WakefulBroadcastReceiver {

	@SuppressWarnings("ConstantConditions")
	public static final String INTENT_ACTION =
	  BuildConfig.DEV ? "com.livae.ff.app.dev.intent.RECEIVE" : "com.livae.ff.app.intent.RECEIVE";

	private static final String TAG = "GCM_ENABLED";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, intent.getExtras().toString());
		ComponentName comp =
		  new ComponentName(context.getPackageName(), CloudMessagesService.class.getName());
		Intent originalIntent;
		originalIntent = intent.getParcelableExtra(CloudMessagesReceiver.EXTRA_ORIGINAL_INTENT);
		startWakefulService(context, originalIntent.setComponent(comp));
		setResultCode(Activity.RESULT_OK);
	}
}
