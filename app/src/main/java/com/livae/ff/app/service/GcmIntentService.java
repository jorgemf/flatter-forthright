package com.livae.ff.app.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.livae.ff.app.receiver.GcmBroadcastReceiver;

public class GcmIntentService extends IntentService {

	public static final int NOTIFICATION_ID = 1;

	private static final String LOG_TAG = "GCM_NOTIFICATIONS";

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			/*
			 * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
			switch (messageType) {
				case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
//					sendNotification("Send error: " + extras.toString());
					break;
				case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
//					sendNotification("Deleted messages on server: " + extras.toString());
					break;
				case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
					Log.i(LOG_TAG, "Received: " + extras.toString());
					sendNotification("Received: " + extras.toString());
					break;
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(String msg) {
//		mNotificationManager =
//		 (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//		PendingIntent contentIntent =
//		 PendingIntent.getActivity(this, 0, new Intent(this, DemoActivity.class), 0);
//
//		NotificationCompat.Builder mBuilder =
//		 new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_stat_gcm)
//		  .setContentTitle("GCM Notification")
//		  .setStyle(new NotificationCompat.BigTextStyle().bigText(msg)).setContentText(msg);
//
//		mBuilder.setContentIntent(contentIntent);
//		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}