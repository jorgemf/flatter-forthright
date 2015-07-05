package com.livae.ff.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.livae.ff.app.R;
import com.livae.ff.common.Constants.ChatType;

public class ChatPrivateActivity extends AbstractChatActivity {

	public static void startChatPrivate(@NonNull ChatsActivity chatsActivity, @NonNull Long phone,
										@NonNull String displayName) {
		start(chatsActivity, ChatType.PRIVATE, null, phone, displayName, null, null);
	}

	public static void startChatAnonymous(@NonNull ChatsActivity chatsActivity, @NonNull Long phone,
										  @NonNull String displayName) {
		start(chatsActivity, ChatType.PRIVATE_ANONYMOUS, null, phone, displayName, null, null);
	}

	public static void startChatSecret(@NonNull ChatsActivity chatsActivity, @NonNull Long phone,
									   @NonNull String displayName) {
		start(chatsActivity, ChatType.SECRET, null, phone, displayName, null, null);
	}

	public static void start(@NonNull Activity activity, @NonNull ChatType chatType,
							 Long conversationId, @NonNull Long phoneNumber, String displayName,
							 String anonymousName, String imageUri) {
		Intent intent = new Intent(activity, ChatPublicActivity.class);
		AbstractChatActivity.startIntent(intent, activity, chatType, conversationId, phoneNumber,
										 displayName, anonymousName, imageUri, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comments);
		onCreated();

//		EditTextDialogFragment dialog = new EditTextDialogFragment() {
//
//			@Override
//			protected void performAction(EditTextDialogFragment dialog, String newText) {
//				Application.appUser().setUserAnonymousName(newText);
//				dialog.dismiss();
//				startChat(activity, conversationId, chatType, phoneNumber, displayName, newText);
//			}
//		};
//		dialog.show(activity, activity.getSupportFragmentManager(), R.string.anonymous_room_title,
//					R.string.anonymous_room_message, R.integer.anonymous_name_max_chars,
//					Application.appUser().getUserAnonymousName());
//		return;

	}

	@Override
	protected void onPause() {
		super.onPause();
//		if (conversationId != null) {
//			new TaskConversationLeave().execute(conversationId, null);
//		new TaskConversationJoin().execute(conversationId,null);
//		}
	}

}
