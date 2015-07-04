package com.livae.ff.app.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.livae.ff.app.R;
import com.livae.ff.app.fragment.CommentsFragment;
import com.livae.ff.app.task.TaskConversationLeave;
import com.livae.ff.common.Constants.ChatType;

public class CommentsActivity extends AbstractActivity {

	public static void startChatFlatter(@NonNull FragmentActivity activity, Long conversationId,
										@NonNull Long userId, @NonNull String userDisplayName,
										String anonymousName) {
		start(activity, conversationId, ChatType.FLATTER, userId, userDisplayName, anonymousName);
	}

	public static void startChatForthright(@NonNull FragmentActivity activity, Long conversationId,
										   @NonNull Long userId, String userDisplayName,
										   String anonymousName) {
		start(activity, conversationId, ChatType.FORTHRIGHT, userId, userDisplayName,
			  anonymousName);
	}

	public static void start(@NonNull Activity activity, Long conversationId,
							 @NonNull ChatType chatType, @NonNull Long phoneNumber,
							 String displayName, String roomName) {
		Intent intent = new Intent(activity, CommentsActivity.class);
		intent.putExtra(EXTRA_CONVERSATION_ID, conversationId);
		intent.putExtra(EXTRA_CHAT_TYPE, chatType);
		intent.putExtra(EXTRA_DISPLAY_NAME, displayName);
		intent.putExtra(EXTRA_ROOM_NAME, roomName);
		intent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity)
														  .toBundle());
		} else {
			activity.startActivity(intent);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(EXTRA_CONVERSATION_ID, conversationId);
		outState.putSerializable(EXTRA_CHAT_TYPE, chatType);
		outState.putString(EXTRA_DISPLAY_NAME, displayName);
		outState.putString(EXTRA_ROOM_NAME, roomName);
		outState.putLong(EXTRA_PHONE_NUMBER, phoneNumber);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		if (anonymousName == null) {
//			EditTextDialogFragment dialog = new EditTextDialogFragment() {
//
//				@Override
//				protected void performAction(EditTextDialogFragment dialog, String newText) {
//					Application.appUser().setUserAnonymousName(newText);
//					dialog.dismiss();
//					startChat(activity, conversationId, chatType, phoneNumber, displayName,
//							  newText);
//				}
//			};
//			dialog.show(activity, activity.getSupportFragmentManager(),
//						R.string.anonymous_name_title, R.string.anonymous_name_message,
//						R.integer.anonymous_name_max_chars,
//						Application.appUser().getUserAnonymousName());
//			return;
//		}

		setContentView(R.layout.activity_comments);
		Bundle extras = getIntent().getExtras();
		if (savedInstanceState != null) {
			extras = savedInstanceState;
		}
		// load data
		conversationId = extras.getLong(EXTRA_CONVERSATION_ID, 0);
		if (conversationId == 0) {
			conversationId = null;
		}
		displayName = extras.getString(EXTRA_DISPLAY_NAME, null);
		roomName = extras.getString(EXTRA_ROOM_NAME, null);
		phoneNumber = extras.getLong(EXTRA_PHONE_NUMBER, 0);
		if (phoneNumber == 0) {
			phoneNumber = null;
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (conversationId != null) {
			new TaskConversationLeave().execute(conversationId, null);
//		new TaskConversationJoin().execute(conversationId,null);
		}
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		if (conversationId != null) {
			if (fragment instanceof CommentsFragment) {
				CommentsFragment commentsFragment = (CommentsFragment) fragment;
				commentsFragment.setConversationType(chatType);
				commentsFragment.setConversationPhone(phoneNumber);
				commentsFragment.setAnonymousNick(roomName);
				commentsFragment.setConversationId(conversationId);
			}
		}
	}
}
