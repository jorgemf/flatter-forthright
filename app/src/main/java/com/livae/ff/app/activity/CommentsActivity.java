package com.livae.ff.app.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.dialog.EditTextDialogFragment;
import com.livae.ff.common.Constants.ChatType;

public class CommentsActivity extends AbstractActivity {

	private static final String EXTRA_CONVERSATION_ID = "EXTRA_CONVERSATION_ID";

	private static final String EXTRA_CHAT_TYPE = "EXTRA_CHAT_TYPE";

	private static final String EXTRA_DISPLAY_NAME = "EXTRA_DISPLAY_NAME";

	private static final String EXTRA_ROOM_NAME = "EXTRA_ROOM_NAME";

	private static final String EXTRA_PHONE_NUMBER = "EXTRA_PHONE_NUMBER";

	private static final String EXTRA_LAST_MESSAGE_DATE = "EXTRA_LAST_MESSAGE_DATE";

	public static void startChat(@NonNull FragmentActivity activity, @NonNull ChatType chatType,
								 @NonNull Long phoneNumber, @NonNull String displayName) {
		startChat(activity, null, chatType, phoneNumber, displayName, null);
	}

	public static void startChat(@NonNull FragmentActivity activity, @NonNull ChatType chatType,
								 @NonNull Long phoneNumber, @NonNull String displayName,
								 String anonymousName) {
		startChat(activity, null, chatType, phoneNumber, displayName, anonymousName);
	}

	public static void startChat(@NonNull final FragmentActivity activity,
								 final Long conversationId, @NonNull final ChatType chatType,
								 @NonNull final Long phoneNumber, @NonNull final String displayName,
								 final String anonymousName) {

		if (anonymousName == null && (chatType == ChatType.PRIVATE_ANONYMOUS ||
									  chatType == ChatType.FORTHRIGHT ||
									  chatType == ChatType.FLATTER)) {
			EditTextDialogFragment dialog = new EditTextDialogFragment() {

				@Override
				protected void performAction(EditTextDialogFragment dialog, String newText) {
					Application.appUser().setUserAnonymousName(newText);
					dialog.dismiss();
					startChat(activity, conversationId, chatType, phoneNumber, displayName,
							  newText);
				}
			};
			if (chatType == ChatType.PRIVATE_ANONYMOUS) {
				dialog.show(activity, activity.getSupportFragmentManager(),
							R.string.anonymous_room_title, R.string.anonymous_room_message,
							R.integer.anonymous_name_max_chars,
							Application.appUser().getUserAnonymousName());
			} else {
				dialog.show(activity, activity.getSupportFragmentManager(),
							R.string.anonymous_name_title, R.string.anonymous_name_message,
							R.integer.anonymous_name_max_chars,
							Application.appUser().getUserAnonymousName());
			}
			return;
		}
		start(activity, conversationId, chatType, displayName, anonymousName, phoneNumber, null);
	}

	public static void start(Activity activity, Long conversationId, ChatType chatType,
							 String displayName, String roomName, Long phoneNumber,
							 Long lastMessageDate) {
		if (conversationId == null && (phoneNumber == null || chatType == null)) {
			throw new RuntimeException("Not enough data to start the conversation");
		}
		Intent intent = new Intent(activity, CommentsActivity.class);
		intent.putExtra(EXTRA_CONVERSATION_ID, conversationId);
		intent.putExtra(EXTRA_CHAT_TYPE, chatType);
		intent.putExtra(EXTRA_DISPLAY_NAME, displayName);
		intent.putExtra(EXTRA_ROOM_NAME, roomName);
		intent.putExtra(EXTRA_PHONE_NUMBER, phoneNumber);
		intent.putExtra(EXTRA_LAST_MESSAGE_DATE, lastMessageDate);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity)
														  .toBundle());
		} else {
			activity.startActivity(intent);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Analytics.screen(Analytics.Screen.COMMENTS);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set up the action bar.
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
//		actionBar.setTitle(R.string.activity_about);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

}
