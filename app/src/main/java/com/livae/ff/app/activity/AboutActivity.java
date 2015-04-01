package com.livae.ff.app.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.livae.apphunt.app.Analytics;
import com.livae.apphunt.app.AppUser;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.R;
import com.livae.apphunt.app.utils.DeviceUtils;
import com.livae.apphunt.app.utils.IntentUtils;

public class AboutActivity extends AbstractActivity implements View.OnClickListener {

	public static void start(Activity activity) {
		Intent intent = new Intent(activity, AboutActivity.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity)
														  .toBundle());
		} else {
			activity.startActivity(intent);
		}
	}

	public static void sendFeedback(Context context) {
		Uri uri = Uri.fromParts("mailto", context.getString(R.string.feedback_email), null);
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		AppUser appUser = Application.appUser();
		Long userId = appUser.getUserId();
		intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.feedback_email_subject) +
											  " [" + DeviceUtils.getApplicationVersionString() +
											  "-" + Build.VERSION.SDK_INT + "-" + Build.MODEL +
											  "-" + Build.DEVICE + " - " + userId + "]");
		intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.feedback_email_body));
		context.startActivity(Intent.createChooser(intent,
												   context.getString(R.string.send_feedback)));
	}

	@Override
	protected void onResume() {
		super.onResume();
		Analytics.screen(Analytics.Screen.ABOUT);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		// Set up the action bar.
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(R.string.activity_about);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		findViewById(R.id.button_share_this_app).setOnClickListener(this);
		findViewById(R.id.button_details_rate_app).setOnClickListener(this);
		findViewById(R.id.button_details_feedback).setOnClickListener(this);
		findViewById(R.id.button_google_community).setOnClickListener(this);
		findViewById(R.id.button_details_terms_conditions).setOnClickListener(this);

		TextView versionTextView = (TextView) findViewById(R.id.about_app_version);
		versionTextView.setText(getString(R.string.app_version,
										  DeviceUtils.getApplicationVersionString()));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_share_this_app:
				Analytics.event(Analytics.Category.USER, Analytics.Action.SHARED_APP);
				IntentUtils.shareApp(this, getPackageName());
				break;
			case R.id.button_details_rate_app:
				Analytics.event(Analytics.Category.USER, Analytics.Action.REVIEW_APP);
				IntentUtils.openMarket(this, getPackageName());
				break;
			case R.id.button_details_feedback:
				Analytics.event(Analytics.Category.USER, Analytics.Action.SEND_FEEDBACK);
				sendFeedback(this);
				break;
			case R.id.button_google_community:
				Analytics.event(Analytics.Category.USER, Analytics.Action.VISITED_COMMUNITY);
				IntentUtils.launchGooglePlusCommunity(this);
				break;
			case R.id.button_details_terms_conditions:
				Analytics.event(Analytics.Category.USER, Analytics.Action.VISITED_TERMS);
				String urlTermsConditions = getString(R.string.url_terms_conditions);
				String title = getString(R.string.activity_about);
				WebViewActivity.start(this, urlTermsConditions, title);
				break;
		}
	}
}
