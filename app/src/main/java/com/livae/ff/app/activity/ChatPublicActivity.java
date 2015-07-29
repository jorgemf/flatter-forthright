package com.livae.ff.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.livae.ff.app.R;
import com.livae.ff.common.Constants.ChatType;

import javax.annotation.Nonnull;

public class ChatPublicActivity extends AbstractChatActivity {

	public static void startChatFlatter(@Nonnull FragmentActivity activity, Long conversationId,
										@Nonnull Long userId, @Nonnull String userDisplayName,
										String anonymousName, String imageUri) {
		start(activity, ChatType.FLATTER, conversationId, userId, userDisplayName, anonymousName,
			  imageUri);
	}

	public static void startChatForthright(@Nonnull FragmentActivity activity, Long conversationId,
										   @Nonnull Long userId, String userDisplayName,
										   String anonymousName, String imageUri) {
		start(activity, ChatType.FORTHRIGHT, conversationId, userId, userDisplayName, anonymousName,
			  imageUri);
	}

	private static void start(@Nonnull Activity activity, @Nonnull ChatType chatType,
							  Long conversationId, @Nonnull Long phoneNumber, String displayName,
							  String anonymousName, String imageUri) {
		Intent intent = new Intent(activity, ChatPublicActivity.class);
		AbstractChatActivity.startIntent(intent, activity, chatType, conversationId, phoneNumber,
										 displayName, anonymousName, imageUri, null, null, null,
										 null, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_public);
		onCreated();
	}
}
