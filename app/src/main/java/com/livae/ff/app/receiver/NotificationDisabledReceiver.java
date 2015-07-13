package com.livae.ff.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.livae.ff.app.Application;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.utils.NotificationUtil;
import com.livae.ff.common.model.Notification;

public class NotificationDisabledReceiver extends BroadcastReceiver {

	private static final String TAG = "GCM_DISABLED";

	private final IntentFilter intentFilter;

	private CloudMessagesDisabledListener listener;

	public NotificationDisabledReceiver() {
		intentFilter = new IntentFilter();
		intentFilter.setPriority(10);
		intentFilter.addAction(NotificationReceiver.INTENT_ACTION);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent originalIntent;
		originalIntent = intent.getParcelableExtra(CloudMessagesReceiver.EXTRA_ORIGINAL_INTENT);
		Bundle extras = originalIntent.getExtras();
		Notification notification = NotificationUtil.parseNotification(extras);
		Log.i(TAG, extras.toString());
		if (notification != null) {
			Model model = Application.model();
			model.parse(notification);
			model.save();
			if (listener != null) {
				if (listener.onNotificationReceived(notification)) {
					abortBroadcast();
				}
			}
		}
	}

	public void register(Context context) {
		context.registerReceiver(this, intentFilter);
	}

	public void unregister(Context context) {
		try {
			context.unregisterReceiver(this);
		} catch (IllegalArgumentException ignore) {
			// it happens when the theme changes
		}
	}

	public void setListener(CloudMessagesDisabledListener listener) {
		this.listener = listener;
	}

	public static interface CloudMessagesDisabledListener {

		public boolean onNotificationReceived(Notification notification);
	}
}
