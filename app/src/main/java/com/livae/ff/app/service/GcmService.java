package com.livae.ff.app.service;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.livae.ff.app.receiver.NotificationReceiver;

public class GcmService extends GcmListenerService {

	private static final String LOG_TAG = "GcmService";

	@Override
	public void onMessageReceived(String from, Bundle data) {
		Log.i(LOG_TAG, "New gcm message from: " + from + " data: " + data);
		Intent intentNotification = new Intent(NotificationReceiver.INTENT_ACTION);
		intentNotification.putExtras(data);
		sendOrderedBroadcast(intentNotification, null);
	}
}
