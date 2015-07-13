package com.livae.ff.app.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.receiver.NotificationReceiver;
import com.livae.ff.app.settings.Notifications;
import com.livae.ff.app.settings.Settings;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.utils.NotificationUtil;
import com.livae.ff.common.Constants.ChatType;
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
						ChatType chatType = ChatType.valueOf(conversationType);

						switch (chatType) {
							case FORTHRIGHT:
								if (notifications.isCommentsForthrightMe()) {
									notifyChatsPublic(context, ChatType.FORTHRIGHT);
								}
								break;
							case FLATTER:
								if (notifications.isCommentsFlatteredMe()) {
									notifyChatsPublic(context, ChatType.FLATTER);
								}
								break;
							case PRIVATE:
							case SECRET:
							case PRIVATE_ANONYMOUS:
								if (notifications.isCommentsChat()) {
									notifyChatsPrivate(context);
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

	private static void notifyChatsPublic(Context context, ChatType chatType) {
		final Resources res = context.getResources();
		final ContentResolver contentResolver = context.getContentResolver();
		Uri uri = ConversationsProvider.getUriCommentsConversations();
		final String[] projection = {Table.Comment.COMMENT};
		final String selection =
		  Table.Comment.DATE + ">" + Table.Conversation.LAST_ACCESS + " AND " +
		  Table.Conversation.TYPE + "=?";
		final String[] selectionArgs = {chatType.name()};
		final String order = "-" + Table.Comment.DATE;
		Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, order);
		if (cursor.moveToFirst()) {
			NotificationCompat.Builder builder;
			builder = NotificationUtil.getDefaultNotificationBuilder(context);
			CharSequence title = "";
			switch (chatType) {
				case FORTHRIGHT:
					builder.setColor(res.getColor(R.color.deep_purple));
					title = res.getText(R.string.notifications_forthright_title);
					break;
				case FLATTER:
					builder.setColor(res.getColor(R.color.pink));
					title = res.getText(R.string.notifications_flatter_title);
					break;
			}
			builder.setContentTitle(title);
			int totalComments = cursor.getCount();
			int iComment = cursor.getColumnIndex(Table.Comment.COMMENT);
			if (totalComments == 1) {
				String comment = cursor.getString(iComment);
				builder.setContentText(comment);
				NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);
				style.setBigContentTitle(title);
				style.setSummaryText(comment);
				builder.setStyle(style);
			} else {
				NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
				int index = 0;
				do {
					String comment = cursor.getString(iComment);
					style.addLine(comment);
					index++;
				} while (cursor.moveToNext() && index < Settings.Notifications.MAXIMUM_MESSAGES);
				int unreadMore = totalComments - Settings.Notifications.MAXIMUM_MESSAGES;
				if (unreadMore > 0) {
					style.setSummaryText(res.getString(R.string.notifications_summary_comments,
													   unreadMore));
				}
				builder.setContentText(res.getString(R.string.notifications_summary_comments,
													 totalComments));
				builder.setNumber(totalComments);
				builder.setStyle(style);
			}
			NotificationManager manager;
			manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			switch (chatType) {
				case FORTHRIGHT:
					manager.notify(Settings.Notifications.ID_CHAT_PUBLIC_FORTHRIGHT,
								   builder.build());
					break;
				case FLATTER:
					manager.notify(Settings.Notifications.ID_CHAT_PUBLIC_FLATTER, builder.build());
					break;
			}
		}
		cursor.close();
	}

	private static void notifyChatsPrivate(Context context) {
		final Resources res = context.getResources();
		final ContentResolver contentResolver = context.getContentResolver();
		Uri uri = ConversationsProvider.getUriCommentsConversations();
		final String[] projection = {Table.Comment.COMMENT, Table.LocalUser.CONTACT_NAME,
									 Table.Conversation.TYPE, Table.Conversation.ROOM_NAME};
		final String selection =
		  Table.Comment.DATE + ">" + Table.Conversation.LAST_ACCESS + " AND " +
		  Table.Conversation.TYPE + " IN (?,?,?)";
		final String[] selectionArgs = {ChatType.PRIVATE.name(), ChatType.SECRET.name(),
										ChatType.PRIVATE_ANONYMOUS.name()};
		final String order = "-" + Table.Comment.DATE;
		Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, order);
		if (cursor.moveToFirst()) {
			NotificationCompat.Builder builder;
			builder = NotificationUtil.getDefaultNotificationBuilder(context);
			CharSequence title = res.getText(R.string.notifications_chat_private_title);
			builder.setContentTitle(title);
			int totalComments = cursor.getCount();
			int iComment = cursor.getColumnIndex(Table.Comment.COMMENT);
			int iDisplayName = cursor.getColumnIndex(Table.LocalUser.CONTACT_NAME);
			int iConversationType = cursor.getColumnIndex(Table.Conversation.TYPE);
			int iRoomName = cursor.getColumnIndex(Table.Conversation.ROOM_NAME);
			if (totalComments == 1) {
				String comment = cursor.getString(iComment);
				String displayName = cursor.getString(iDisplayName);
				String roomName = cursor.getString(iRoomName);
				ChatType chatType = ChatType.valueOf(cursor.getString(iConversationType));
				Spannable text = createCommentLine(res, chatType, comment, displayName, roomName);
				builder.setContentText(text);
				NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);
				style.setBigContentTitle(title);
				style.setSummaryText(text);
				builder.setStyle(style);
			} else {
				NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
				int index = 0;
				do {
					String comment = cursor.getString(iComment);
					String displayName = cursor.getString(iDisplayName);
					String roomName = cursor.getString(iRoomName);
					ChatType chatType = ChatType.valueOf(cursor.getString(iConversationType));
					style.addLine(createCommentLine(res, chatType, comment, displayName, roomName));
					index++;
				} while (cursor.moveToNext() && index < Settings.Notifications.MAXIMUM_MESSAGES);
				int unreadMore = totalComments - Settings.Notifications.MAXIMUM_MESSAGES;
				if (unreadMore > 0) {
					style.setSummaryText(res.getString(R.string.notifications_summary_comments,
													   unreadMore));
				}
				builder.setContentText(res.getString(R.string.notifications_summary_comments,
													 totalComments));
				builder.setNumber(totalComments);
				builder.setStyle(style);
			}
			NotificationManager manager;
			manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(Settings.Notifications.ID_CHAT_PRIVATE, builder.build());
		}
		cursor.close();
	}

	private static Spannable createCommentLine(Resources resources, ChatType chatType,
											   String comment, String displayName,
											   String roomName) {
		String title = "";
		String text = "";
		String italicText = "";
		switch (chatType) {
			case PRIVATE:
				title = displayName;
				text = comment;
				break;
			case SECRET:
				title = displayName;
				italicText = resources.getString(R.string.notifications_secret_content);
				break;
			case PRIVATE_ANONYMOUS:
				title = roomName;
				text = comment;
				break;
		}
		return NotificationUtil.makeNotificationLine(title, text, italicText);
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
