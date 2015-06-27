package com.livae.ff.app.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.receiver.NotificationReceiver;
import com.livae.ff.app.settings.Notifications;
import com.livae.ff.app.settings.Settings;
import com.livae.ff.app.utils.NotificationUtil;
import com.livae.ff.common.Constants;
import com.livae.ff.common.Constants.PushNotificationType;
import com.livae.ff.common.model.Notification;
import com.livae.ff.common.model.NotificationComment;

public class CloudMessagesService extends IntentService {

	public static final String LOG_TAG = "GCM_NOTIFICATIONS";

	public CloudMessagesService() {
		super("CloudMessagesService");
	}

	public static void processNotification(Context context, Bundle extras, boolean notify) {
		PushNotificationType type = NotificationUtil.getNotificationType(extras);
		if (type != null) {
			Notifications notifications = Application.appUser().getNotifications();
			Notification notification = NotificationUtil.parseNotification(extras);
			Model model = Application.model();
			model.parse(notification);
			model.save();
			switch (type) {
				case COMMENT:
					NotificationComment notificationComment = (NotificationComment) notification;
					String conversationType = notificationComment.getConversationType();
					try {
						Constants.ChatType chatType = Constants.ChatType.valueOf(conversationType);

						switch (chatType) {
							case FORTHRIGHT:
								if (notifications.isCommentsForthrightMe()) {
									// TODO
								}
								break;
							case FLATTER:
								if (notifications.isCommentsFlatteredMe()) {
									// TODO
								}
								break;
							case PRIVATE:
							case SECRET:
							case PRIVATE_ANONYMOUS:
								if (notifications.isCommentsChat()) {
									// TODO
//									notifyChats(context);
								}
								break;
						}
					} catch (IllegalArgumentException ignore) {

					}
					break;
			}
		}
	}

	private static void notifyMessage(Context context, String message) {
		NotificationCompat.Builder builder;
		builder = NotificationUtil.getDefaultNotificationBuilder(context);
		builder.setContentText(message);
		builder.setCategory("Message");
		builder.setLargeIcon(NotificationUtil.getLargeDefaultIcon(context));

		NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);
		style.setBigContentTitle(message);

		builder.setStyle(style);

		NotificationManager manager;
		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(Settings.Notifications.ID_MESSAGE, builder.build());
	}

	private static void notifyChats(Context context) {
		// TODO
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			switch (messageType) {
				case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
					Log.i(LOG_TAG, "Error: " + extras.toString());
					break;
				case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
					Log.i(LOG_TAG, "Deleted: " + extras.toString());
					break;
				case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
					Log.i(LOG_TAG, "Received: " + extras.toString());
					processNotification(this, extras, true);
					break;
			}
		}
		NotificationReceiver.completeWakefulIntent(intent);
	}
}