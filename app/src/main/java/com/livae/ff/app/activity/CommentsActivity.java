package com.livae.ff.app.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.R;
import com.livae.ff.common.Constants.ChatType;

public class CommentsActivity extends AbstractActivity implements View.OnClickListener {

	public static void startChat(Activity activity, ChatType chatType, Long phoneNumber,
								 String displayName) {
		startChat(activity, null, chatType, phoneNumber, displayName, null);
	}

	public static void startChat(Activity activity, ChatType chatType, Long phoneNumber,
								 String displayName, String anonymousName) {
		startChat(activity, null, chatType, phoneNumber, displayName, anonymousName);
	}

	public static void startChat(Activity activity, Long conversationId, ChatType chatType,
								 Long phoneNumber, String displayName, String anonymousName) {
		if (conversationId == null) {
			// TODO
		} else {
			// TODO
		}
	}

	public static void start(Activity activity, @NonNull Long conversationId,
							 @NonNull ChatType chatType, @NonNull String roomName) {
		Intent intent = new Intent(activity, CommentsActivity.class);
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

	@Override
	public void onClick(View v) {

	}

}
