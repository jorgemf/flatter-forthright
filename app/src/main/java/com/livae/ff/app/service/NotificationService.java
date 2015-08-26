package com.livae.ff.app.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.R;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.receiver.NotificationReceiver;
import com.livae.ff.app.settings.Notifications;
import com.livae.ff.app.settings.Settings;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.ui.activity.AbstractChatActivity;
import com.livae.ff.app.ui.activity.ChatsActivity;
import com.livae.ff.app.utils.Debug;
import com.livae.ff.app.utils.NotificationUtil;
import com.livae.ff.common.Constants.ChatType;
import com.livae.ff.common.Constants.PushNotificationType;
import com.livae.ff.common.model.Notification;
import com.livae.ff.common.model.NotificationComment;

public class NotificationService extends IntentService {

	public static final String LOG_TAG = "GCM_NOTIFICATIONS";

	public NotificationService() {
		super("CloudMessagesService");
	}

	public static void processNotification(Context context, Bundle extras) {
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
					//noinspection PointlessBooleanExpression,ConstantConditions
					if (!notificationComment.getIsMe() && !BuildConfig.TEST) {
						String conversationType = notificationComment.getConversationType();
						// increase unread count of conversation
						Long conversationId = notificationComment.getConversationId();
						Uri uriConversation =
						  ConversationsProvider.getUriConversationIncreaseUnread(conversationId);
						context.getContentResolver().update(uriConversation, null, null, null);
						// notify
						try {
							ChatType chatType = ChatType.valueOf(conversationType);

							switch (chatType) {
								case FORTHRIGHT:
									if (conversationId.equals(Application.appUser()
																		 .getChats()
																		 .getChatForthrightId())) {
										Application.appUser()
												   .getChats()
												   .increaseChatForthrightUnread();
									}
									notifyChatsPublic(context, ChatType.FORTHRIGHT, true);
									break;
								case FLATTER:
									if (conversationId.equals(Application.appUser()
																		 .getChats()
																		 .getChatFlatterId())) {
										Application.appUser()
												   .getChats()
												   .increaseChatFlatterUnread();
									}
									notifyChatsPublic(context, ChatType.FLATTER, true);
									break;
								case PRIVATE:
								case SECRET:
								case PRIVATE_ANONYMOUS:
									if (notifications.isCommentsChat()) {
										notifyChatsPrivate(context, true);
									}
									break;
							}
						} catch (IllegalArgumentException ignore) {

						}
						break;
					}
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

	public static void notifyChatsPublic(Context context, ChatType chatType, boolean sound) {
		final Resources res = context.getResources();
		final ContentResolver contentResolver = context.getContentResolver();
		Uri uri = ConversationsProvider.getUriCommentsConversations();
		final String[] projection =
		  {Table.Comment.COMMENT, Table.Comment.USER_ALIAS, Table.Comment.USER_ANONYMOUS_ID,
		   Table.Comment.CONVERSATION_ID, Table.Conversation.LAST_ACCESS,
		   Table.Conversation.LAST_MESSAGE_DATE, Table.Conversation.PHONE,
		   Table.Conversation.ROOM_NAME, Table.Conversation.NOTIFICATION_COLOR,
		   Table.Conversation.NOTIFICATION_SOUND, Table.Conversation.NOTIFICATION_MUTED,
		   Table.LocalUser.CONTACT_NAME, Table.LocalUser.IMAGE_URI};
		final String selection = "( " + Table.Conversation.LAST_ACCESS + " IS NULL OR " +
								 Table.Comment.DATE + ">" + Table.Conversation.LAST_ACCESS +
								 " ) AND " + Table.Conversation.TYPE + "=? AND " +
								 Table.Comment.IS_ME + "=0";
		final String[] selectionArgs = {chatType.name()};
		final String order = "-" + Table.Comment.DATE;
		Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, order);
		NotificationManager manager;
		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (cursor.moveToFirst()) {
			NotificationCompat.Builder builder;
			builder = NotificationUtil.getDefaultNotificationBuilder(context);
			if (!sound) {
				builder.setSound(null);
				builder.setDefaults(android.app.Notification.DEFAULT_LIGHTS);
			}
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
			int iAlias = cursor.getColumnIndex(Table.Comment.USER_ALIAS);
			int iConversationId = cursor.getColumnIndex(Table.Comment.CONVERSATION_ID);
			int iLastAccess = cursor.getColumnIndex(Table.Conversation.LAST_ACCESS);
			int iLastMessageDate = cursor.getColumnIndex(Table.Conversation.LAST_MESSAGE_DATE);
			int iPhoneNumber = cursor.getColumnIndex(Table.Conversation.PHONE);
			int iDisplayName = cursor.getColumnIndex(Table.Conversation.ROOM_NAME);
			int iNotificationSound = cursor.getColumnIndex(Table.Conversation.NOTIFICATION_SOUND);
			int iNotificationColor = cursor.getColumnIndex(Table.Conversation.NOTIFICATION_COLOR);
			int iNotificationMuted = cursor.getColumnIndex(Table.Conversation.NOTIFICATION_MUTED);
			int iContactName = cursor.getColumnIndex(Table.LocalUser.CONTACT_NAME);
			int iImageUri = cursor.getColumnIndex(Table.LocalUser.IMAGE_URI);
			Intent intent;
			if (totalComments == 1) {
				String comment = cursor.getString(iComment);
				String alias = cursor.getString(iAlias);
				Spannable text = NotificationUtil.makeNotificationLine(alias, comment, "");
				builder.setContentText(text);
				NotificationCompat.BigTextStyle style =
				  new NotificationCompat.BigTextStyle(builder);
				style.setBigContentTitle(title);
				style.bigText(text);
				builder.setStyle(style);
				Long conversationId = cursor.getLong(iConversationId);
				Long lastAccess = cursor.getLong(iLastAccess);
				Long lastMessageDate = cursor.getLong(iLastMessageDate);
				Long phoneNumber = cursor.getLong(iPhoneNumber);
				String displayName = cursor.getString(iDisplayName);
				String roomName = cursor.getString(iContactName);
				String userImageUri = cursor.getString(iImageUri);
				if(Application.appUser().getUserPhone().equals(phoneNumber)){
					displayName = roomName;
				}
				setCustomization(sound, cursor, builder, iNotificationSound, iNotificationColor,
								 iNotificationMuted);
				intent =
				  AbstractChatActivity.createIntent(context, chatType, conversationId, phoneNumber,
													displayName, roomName, userImageUri, null,
													lastAccess, lastMessageDate, totalComments,
													null, null);
			} else {
				NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
				int index = 0;
				Long conversationId = cursor.getLong(iConversationId);
				do {
					long cId = cursor.getLong(iConversationId);
					if (index < Settings.Notifications.MAXIMUM_MESSAGES) {
						String comment = cursor.getString(iComment);
						String alias = cursor.getString(iAlias);
						Spannable text = NotificationUtil.makeNotificationLine(alias, comment, "");
						style.addLine(text);
						index++;
					}
					if (conversationId != null && conversationId != cId) {
						conversationId = null;
					}
				} while (cursor.moveToNext() && (index < Settings.Notifications.MAXIMUM_MESSAGES ||
												 conversationId != null));
				int unreadMore = totalComments - Settings.Notifications.MAXIMUM_MESSAGES;
				if (unreadMore > 0) {
					style.setSummaryText(res.getString(R.string
														 .notifications_summary_comments_more,
													   unreadMore));
				}
				builder.setContentText(res.getString(R.string.notifications_summary_comments,
													 totalComments));
				builder.setStyle(style);
				if (conversationId != null) {
					cursor.moveToFirst();
					Long lastAccess = cursor.getLong(iLastAccess);
					Long lastMessageDate = cursor.getLong(iLastMessageDate);
					Long phoneNumber = cursor.getLong(iPhoneNumber);
					String displayName = cursor.getString(iDisplayName);
					String roomName = cursor.getString(iContactName);
					String userImageUri = cursor.getString(iImageUri);
					if(Application.appUser().getUserPhone().equals(phoneNumber)){
						displayName = roomName;
					}
					setCustomization(sound, cursor, builder, iNotificationSound,
									 iNotificationColor,
									 iNotificationMuted);
					intent = AbstractChatActivity.createIntent(context, chatType, conversationId,
															   phoneNumber, displayName, roomName,
															   userImageUri, null, lastAccess,
															   lastMessageDate, totalComments,
															   null,
															   null);
				} else {
					intent = new Intent(context, ChatsActivity.class);
				}
			}
			PendingIntent pending =
			  PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(pending);
			switch (chatType) {
				case FORTHRIGHT:
					manager.notify(Settings.Notifications.ID_CHAT_PUBLIC_FORTHRIGHT,
								   builder.build());
					break;
				case FLATTER:
					manager.notify(Settings.Notifications.ID_CHAT_PUBLIC_FLATTER, builder.build());
					break;
			}
		} else {
			switch (chatType) {
				case FORTHRIGHT:
					manager.cancel(Settings.Notifications.ID_CHAT_PUBLIC_FORTHRIGHT);
					break;
				case FLATTER:
					manager.cancel(Settings.Notifications.ID_CHAT_PUBLIC_FLATTER);
					break;
			}
		}
		cursor.close();
	}

	public static void notifyChatsPrivate(Context context, boolean sound) {
		final Resources res = context.getResources();
		final ContentResolver contentResolver = context.getContentResolver();
		Uri uri = ConversationsProvider.getUriCommentsConversations();
		final String[] projection =
		  {Table.Comment.COMMENT, Table.Comment.CONVERSATION_ID, Table.LocalUser.CONTACT_NAME,
		   Table.LocalUser.IMAGE_URI, Table.LocalUser.BLOCKED,
		   Table.LocalUser.ANDROID_RAW_CONTACT_ID, Table.Conversation.TYPE,
		   Table.Conversation.ROOM_NAME, Table.Conversation.LAST_ACCESS,
		   Table.Conversation.LAST_MESSAGE_DATE, Table.Conversation.PHONE,
		   Table.Conversation.ALIAS_ID, Table.Conversation.NOTIFICATION_SOUND,
		   Table.Conversation.NOTIFICATION_COLOR, Table.Conversation.NOTIFICATION_MUTED};
		final String selection =
		  Table.Comment.DATE + ">" + Table.Conversation.LAST_ACCESS + " AND " +
		  Table.Conversation.TYPE + " IN (?,?,?) AND " + Table.Comment.IS_ME + "=0";
		final String[] selectionArgs =
		  {ChatType.PRIVATE.name(), ChatType.SECRET.name(), ChatType.PRIVATE_ANONYMOUS.name()};
		final String order = "-" + Table.Comment.DATE;
		Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, order);
		NotificationManager manager;
		manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (cursor.moveToFirst()) {
			NotificationCompat.Builder builder;
			builder = NotificationUtil.getDefaultNotificationBuilder(context);
			if (!sound) {
				builder.setSound(null);
				builder.setDefaults(android.app.Notification.DEFAULT_LIGHTS);
			}
			CharSequence title = res.getText(R.string.notifications_chat_private_title);
			builder.setContentTitle(title);
			int totalComments = cursor.getCount();
			int iComment = cursor.getColumnIndex(Table.Comment.COMMENT);
			int iDisplayName = cursor.getColumnIndex(Table.LocalUser.CONTACT_NAME);
			int iImageUri = cursor.getColumnIndex(Table.LocalUser.IMAGE_URI);
			int iBlocked = cursor.getColumnIndex(Table.LocalUser.BLOCKED);
			int iRawContactId = cursor.getColumnIndex(Table.LocalUser.ANDROID_RAW_CONTACT_ID);
			int iConversationType = cursor.getColumnIndex(Table.Conversation.TYPE);
			int iRoomName = cursor.getColumnIndex(Table.Conversation.ROOM_NAME);
			int iConversationId = cursor.getColumnIndex(Table.Comment.CONVERSATION_ID);
			int iLastAccess = cursor.getColumnIndex(Table.Conversation.LAST_ACCESS);
			int iLastMessageDate = cursor.getColumnIndex(Table.Conversation.LAST_MESSAGE_DATE);
			int iAliasId = cursor.getColumnIndex(Table.Conversation.ALIAS_ID);
			int iPhone = cursor.getColumnIndex(Table.Conversation.PHONE);
			int iNotificationSound = cursor.getColumnIndex(Table.Conversation.NOTIFICATION_SOUND);
			int iNotificationColor = cursor.getColumnIndex(Table.Conversation.NOTIFICATION_COLOR);
			int iNotificationMuted = cursor.getColumnIndex(Table.Conversation.NOTIFICATION_MUTED);
			Intent intent;
			if (totalComments == 1) {
				String comment = cursor.getString(iComment);
				String displayName = cursor.getString(iDisplayName);
				String roomName = cursor.getString(iRoomName);
				ChatType chatType = ChatType.valueOf(cursor.getString(iConversationType));
				Spannable text = createCommentLine(res, chatType, comment, displayName, roomName);
				builder.setContentText(text);
				NotificationCompat.BigTextStyle style =
				  new NotificationCompat.BigTextStyle(builder);
				style.setBigContentTitle(title);
				style.bigText(text);
				builder.setStyle(style);
				Long conversationId = cursor.getLong(iConversationId);
				Long lastAccess = cursor.getLong(iLastAccess);
				Long lastMessageDate = cursor.getLong(iLastMessageDate);
				Long phone = cursor.getLong(iPhone);
				Long aliasId = cursor.getLong(iAliasId);
				String imageUri = cursor.getString(iImageUri);
				setCustomization(sound, cursor, builder, iNotificationSound, iNotificationColor,
								 iNotificationMuted);
				boolean blocked = cursor.getInt(iBlocked) != 0;
				Long rawContactId =
				  cursor.isNull(iRawContactId) ? null : cursor.getLong(iRawContactId);
				intent = AbstractChatActivity.createIntent(context, chatType, conversationId,
														   phone,
														   displayName, roomName, imageUri,
														   aliasId,
														   lastAccess, lastMessageDate,
														   totalComments, blocked, rawContactId);
			} else {
				NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
				int index = 0;
				Long conversationId = cursor.getLong(iConversationId);
				do {
					if (index < Settings.Notifications.MAXIMUM_MESSAGES) {
						String comment = cursor.getString(iComment);
						String displayName = cursor.getString(iDisplayName);
						String roomName = cursor.getString(iRoomName);
						ChatType chatType = ChatType.valueOf(cursor.getString(iConversationType));
						style.addLine(createCommentLine(res, chatType, comment, displayName,
														roomName));
						index++;
					}
					long cId = cursor.getLong(iConversationId);
					if (conversationId != null && conversationId != cId) {
						conversationId = null;
					}
				} while (cursor.moveToNext() && (index < Settings.Notifications.MAXIMUM_MESSAGES ||
												 conversationId != null));
				int unreadMore = totalComments - Settings.Notifications.MAXIMUM_MESSAGES;
				if (unreadMore > 0) {
					style.setSummaryText(res.getString(R.string.notifications_summary_comments,
													   unreadMore));
				}
				builder.setContentText(res.getString(R.string.notifications_summary_comments,
													 totalComments));
				builder.setStyle(style);
				if (conversationId != null) {
					cursor.moveToFirst();
					ChatType chatType = ChatType.valueOf(cursor.getString(iConversationType));
					Long lastAccess = cursor.getLong(iLastAccess);
					Long lastMessageDate = cursor.getLong(iLastMessageDate);
					Long phone = cursor.getLong(iPhone);
					Long aliasId = cursor.getLong(iAliasId);
					String imageUri = cursor.getString(iImageUri);
					String displayName = cursor.getString(iDisplayName);
					String roomName = cursor.getString(iRoomName);
					setCustomization(sound, cursor, builder, iNotificationSound,
									 iNotificationColor,
									 iNotificationMuted);
					boolean blocked = cursor.getInt(iBlocked) != 0;
					Long rawContactId =
					  cursor.isNull(iRawContactId) ? null : cursor.getLong(iRawContactId);
					intent =
					  AbstractChatActivity.createIntent(context, chatType, conversationId, phone,
														displayName, roomName, imageUri, aliasId,
														lastAccess, lastMessageDate, totalComments,
														blocked, rawContactId);
				} else {
					intent = new Intent(context, ChatsActivity.class);
				}
			}
			PendingIntent pending =
			  PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(pending);
			manager.notify(Settings.Notifications.ID_CHAT_PRIVATE, builder.build());
		} else {
			manager.cancel(Settings.Notifications.ID_CHAT_PRIVATE);
		}
		cursor.close();
	}

	private static void setCustomization(boolean sound,
										 Cursor cursor,
										 NotificationCompat.Builder builder,
										 int iNotificationSound,
										 int iNotificationColor,
										 int iNotificationMuted) {
		String notificationSoundUri =
		  cursor.isNull(iNotificationSound) ? null : cursor.getString(iNotificationSound);
		Integer notificationColor =
		  cursor.isNull(iNotificationColor) ? null : cursor.getInt(iNotificationColor);
		Long notificationMuted =
		  cursor.isNull(iNotificationMuted) ? null : cursor.getLong(iNotificationMuted);
		boolean muted = false;
		if (notificationMuted != null) {
			muted = notificationMuted < 0 || notificationMuted > System.currentTimeMillis();
		}
		if (muted || !sound) {
			builder.setSound(null);
			builder.setVibrate(null);
			builder.setDefaults(android.app.Notification.DEFAULT_LIGHTS);
		} else if (notificationSoundUri != null) {
			builder.setSound(Uri.parse(notificationSoundUri));
			builder.setDefaults(~android.app.Notification.DEFAULT_SOUND);
		}
		if (notificationColor != null) {
			android.app.Notification notification = builder.build();
			builder.setLights(notificationColor, notification.ledOnMS, notification.ledOffMS);
			if (muted || !sound) {
				builder.setDefaults(~android.app.Notification.DEFAULT_ALL);
			} else if (notificationSoundUri != null) {
				builder.setDefaults(android.app.Notification.DEFAULT_VIBRATE);
			} else {
				builder.setDefaults(android.app.Notification.DEFAULT_VIBRATE |
									android.app.Notification.DEFAULT_SOUND);
			}
		}
	}

	private static Spannable createCommentLine(Resources resources,
											   ChatType chatType,
											   String comment,
											   String displayName,
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
		Log.i(LOG_TAG, "New notification");
		processNotification(this, intent.getExtras());
		NotificationReceiver.completeWakefulIntent(intent);
	}
}
