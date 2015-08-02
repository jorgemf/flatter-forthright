package com.livae.ff.app.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.livae.ff.app.R;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.sql.Table;
import com.livae.ff.common.Constants.ChatType;

import javax.annotation.Nonnull;

public class ChatPrivateActivity extends AbstractChatActivity {

	private Long secretConversationId;

	public static void startChatPrivate(@Nonnull ChatsActivity chatsActivity, @Nonnull Long phone,
										@Nonnull String displayName, boolean userBlocked,
										long rawContactId) {
		start(chatsActivity, ChatType.PRIVATE, null, phone, displayName, null, null, null, null,
			  null, userBlocked, rawContactId);
	}

	public static void startChatAnonymous(@Nonnull ChatsActivity chatsActivity, @Nonnull Long phone,
										  @Nonnull String displayName, boolean userBlocked,
										  long rawContactId) {
		start(chatsActivity, ChatType.PRIVATE_ANONYMOUS, null, phone, displayName, null, null, null,
			  null, null, userBlocked, rawContactId);
	}

	public static void startChatSecret(@Nonnull ChatsActivity chatsActivity, @Nonnull Long phone,
									   @Nonnull String displayName, boolean userBlocked,
									   long rawContactId) {
		start(chatsActivity, ChatType.SECRET, null, phone, displayName, null, null, null, null,
			  null, userBlocked, rawContactId);
	}

	public static void start(@Nonnull Activity activity, @Nonnull ChatType chatType,
							 Long conversationId, @Nonnull Long phoneNumber, String displayName,
							 String anonymousName, String imageUri, Long lastAccess,
							 Long lastMessage, Integer unreadMessages, boolean userBlocked,
							 Long rawContactId) {
		Intent intent = new Intent(activity, ChatPrivateActivity.class);
		AbstractChatActivity.startIntent(intent, activity, chatType, conversationId, phoneNumber,
										 displayName, anonymousName, imageUri, null, lastAccess,
										 lastMessage, unreadMessages, userBlocked, rawContactId);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_private);
		onCreated();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (secretConversationId != null) {
			ContentResolver cr = getContentResolver();
			Uri uriComments = ConversationsProvider
								.getUriConversationComments(secretConversationId);
			cr.delete(uriComments, null, null);
			ContentValues values = new ContentValues();
			values.putNull(Table.Conversation.LAST_MESSAGE);
			Uri uriConversation = ConversationsProvider.getUriConversation(secretConversationId);
			cr.update(uriConversation, values, null, null);
		}
	}

	public void setSecretConversationId(Long secretConversationId) {
		this.secretConversationId = secretConversationId;
	}
}
