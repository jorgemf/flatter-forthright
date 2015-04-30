package com.livae.ff.app.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.R;
import com.livae.ff.common.Constants;

public class CommentsActivity extends AbstractActivity implements View.OnClickListener {

	public static void start(Activity activity) {
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
	protected Fragment getFragment(Constants.CommentType fragmentType) {
		return null;
	}

	@Override
	public void onClick(View v) {

	}

}
