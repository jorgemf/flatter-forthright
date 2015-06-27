package com.livae.ff.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CloudMessagesReceiver extends BroadcastReceiver {

	public static final String EXTRA_ORIGINAL_INTENT = "EXTRA_ORIGINAL_INTENT";

	private static final String TAG = "GCM";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getExtras() != null) {
			Log.e(TAG, intent.getExtras().toString());
		}
		Log.i(TAG, "Received: " + intent);
		Intent intentNotification = new Intent(NotificationReceiver.INTENT_ACTION);
		intentNotification.putExtra(EXTRA_ORIGINAL_INTENT, intent);
		context.sendOrderedBroadcast(intentNotification, null);
	}
}