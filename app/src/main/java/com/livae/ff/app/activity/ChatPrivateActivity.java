package com.livae.ff.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.livae.ff.app.R;
import com.livae.ff.common.Constants.ChatType;

import javax.annotation.Nonnull;

public class ChatPrivateActivity extends AbstractChatActivity {

	public static void startChatPrivate(@Nonnull ChatsActivity chatsActivity, @Nonnull Long phone,
										@Nonnull String displayName) {
		start(chatsActivity, ChatType.PRIVATE, null, phone, displayName, null, null);
	}

	public static void startChatAnonymous(@Nonnull ChatsActivity chatsActivity, @Nonnull Long phone,
										  @Nonnull String displayName) {
		start(chatsActivity, ChatType.PRIVATE_ANONYMOUS, null, phone, displayName, null, null);
	}

	public static void startChatSecret(@Nonnull ChatsActivity chatsActivity, @Nonnull Long phone,
									   @Nonnull String displayName) {
		start(chatsActivity, ChatType.SECRET, null, phone, displayName, null, null);
	}

	public static void start(@Nonnull Activity activity, @Nonnull ChatType chatType,
							 Long conversationId, @Nonnull Long phoneNumber, String displayName,
							 String anonymousName, String imageUri) {
		Intent intent = new Intent(activity, ChatPrivateActivity.class);
		AbstractChatActivity.startIntent(intent, activity, chatType, conversationId, phoneNumber,
										 displayName, anonymousName, imageUri, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_private);
		onCreated();

	}

}
