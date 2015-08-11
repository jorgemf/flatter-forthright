package com.livaee.ff.app.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.livae.ff.app.AppUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.provider.ContactsProvider;
import com.livae.ff.app.receiver.CloudMessagesReceiver;
import com.livae.ff.app.receiver.NotificationReceiver;
import com.livae.ff.app.settings.Chats;
import com.livae.ff.app.sql.DBHelper;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.utils.SyncUtils;
import com.livae.ff.common.Constants;
import com.livae.ff.common.model.NotificationComment;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class TestService extends IntentService {

	public static final String EXTRA_ACTION = "EXTRA_ACTION";

	public static final String EXTRA_DATA = "EXTRA_DATA";

	private static final String LOG_TAG = "TEST_SERVICE";

	private static long commentsCounter = 0L;

	public TestService() {
		super("TestService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.hasExtra(EXTRA_ACTION)) {
			ContentValues values;
			User user;
			final AppUser appUser = Application.appUser();
			switch ((ACTION) intent.getSerializableExtra(EXTRA_ACTION)) {
				case REGISTER_MY_PHONE:
					user = (User) intent.getSerializableExtra(EXTRA_DATA);
					DBHelper.clearData(this);
					appUser.clear();
					appUser.setUserPhone(user.phone);
					appUser.setAccessToken("accessToken");
					final Chats appUserChats = appUser.getChats();
					appUserChats.setUserDisplayName(user.name);
					appUserChats.setUserImageUri(user.imageUrl);
					SyncUtils.createAccount(this, user.phone);
					Log.i(LOG_TAG, "my phone registered: " + user.phone);
					break;
				case ADD_USER:
					user = (User) intent.getSerializableExtra(EXTRA_DATA);
					values = new ContentValues();
					values.put(Table.LocalUser.ACCEPTS_PRIVATE, true);
					values.put(Table.LocalUser.IS_MOBILE_NUMBER, true);
					values.put(Table.LocalUser.CONTACT_NAME, user.name);
					values.put(Table.LocalUser.IMAGE_URI, user.imageUrl);
					values.put(Table.LocalUser.PHONE, user.phone);
					getContentResolver().insert(ContactsProvider.getUriContacts(), values);
					Log.i(LOG_TAG, "added user: " + user.name);
					break;
				case POST_MESSAGE:
					Comment comment = (Comment) intent.getSerializableExtra(EXTRA_DATA);
					NotificationComment notificationComment = new NotificationComment();
					notificationComment.setId(commentsCounter++);
					notificationComment.setConversationId(comment.conversationId);
					notificationComment.setConversationType(comment.chatType.name());
					notificationComment.setComment(comment.comment);
					notificationComment.setDate(System.currentTimeMillis() -
												TimeUnit.SECONDS.toMillis(comment.secondsAgo));
					notificationComment.setIsMe(comment.userId == appUser.getUserPhone());
					notificationComment.setUserId(comment.userId);
					notificationComment.setConversationUserId(comment.publicUserId);
					switch (comment.chatType) {
						case FORTHRIGHT:
						case FLATTER:
						case PRIVATE_ANONYMOUS:
							if (comment.alias != null) {
								notificationComment.setAlias(comment.alias);
								notificationComment.setAliasId(comment.alias.hashCode() + 1L);
							}
							break;
					}
					if (comment.secondsAgo > 0) {
						Model model = Application.model();
						model.parse(notificationComment);
						model.save();
					} else {
						Intent originalIntent = new Intent();
						originalIntent.putExtra("t", Constants.PushNotificationType.COMMENT.name
																							  ());

						final Gson gson = new GsonBuilder().create();
						originalIntent.putExtra("m", gson.toJson(notificationComment));
						originalIntent.setAction("com.google.android.c2dm.intent.RECEIVE");
						originalIntent.putExtra("message_type", "gcm");
						Intent intentNotification = new Intent(NotificationReceiver.INTENT_ACTION);
						intentNotification.putExtra(CloudMessagesReceiver.EXTRA_ORIGINAL_INTENT,
													originalIntent);
						sendOrderedBroadcast(intentNotification, null);
					}
					Log.i(LOG_TAG, "post comment: " + comment.comment);
					break;
			}
		}
	}

	public enum ACTION {REGISTER_MY_PHONE, ADD_USER, POST_MESSAGE}

	static public class User implements Serializable {

		public long phone;

		public String name;

		public String imageUrl;

		public User(long phone, String name, String imageUrl) {
			this.phone = phone;
			this.name = name;
			this.imageUrl = imageUrl;
		}
	}

	static public class Comment implements Serializable {

		public long userId;

		public String comment;

		public Constants.ChatType chatType;

		public Long conversationId;

		public String alias;

		public long secondsAgo;

		public Long publicUserId;

		public Comment(long conversationId,
					   Constants.ChatType chatType,
					   long userId,
					   String alias,
					   String comment,
					   long secondsAgo,
					   Long publicUserId) {
			this.conversationId = conversationId;
			this.chatType = chatType;
			this.userId = userId;
			this.alias = alias;
			this.comment = comment;
			this.secondsAgo = secondsAgo;
			this.publicUserId = publicUserId;
		}
	}

}
