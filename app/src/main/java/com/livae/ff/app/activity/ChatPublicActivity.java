package com.livae.ff.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.livae.ff.app.R;
import com.livae.ff.common.Constants.ChatType;

public class ChatPublicActivity extends AbstractChatActivity {

	public static void startChatFlatter(@NonNull FragmentActivity activity, Long conversationId,
										@NonNull Long userId, @NonNull String userDisplayName,
										String anonymousName, String imageUri) {
		start(activity, ChatType.FLATTER, conversationId, userId, userDisplayName, anonymousName,
			  imageUri);
	}

	public static void startChatForthright(@NonNull FragmentActivity activity, Long conversationId,
										   @NonNull Long userId, String userDisplayName,
										   String anonymousName, String imageUri) {
		start(activity, ChatType.FORTHRIGHT, conversationId, userId, userDisplayName, anonymousName,
			  imageUri);
	}

	private static void start(@NonNull Activity activity, @NonNull ChatType chatType,
							  Long conversationId, @NonNull Long phoneNumber, String displayName,
							  String anonymousName, String imageUri) {
		Intent intent = new Intent(activity, ChatPublicActivity.class);
		AbstractChatActivity.startIntent(intent, activity, chatType, conversationId, phoneNumber,
										 displayName, anonymousName, imageUri, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_public);
		onCreated();
	}

}
