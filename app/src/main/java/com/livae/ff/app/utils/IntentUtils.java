package com.livae.ff.app.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.AppUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.Constants;
import com.livae.ff.app.R;
import com.livae.ff.app.activity.WebViewActivity;

public class IntentUtils {

	public static void readTermsAndConditions(Context context) {
		Analytics.event(Analytics.Category.USER, Analytics.Action.VISITED_TERMS);
		String urlTermsConditions = context.getString(R.string.url_terms_conditions);
		String title = context.getString(R.string.activity_about);
		WebViewActivity.start(context, urlTermsConditions, title);
	}

	public static void sendFeedback(Context context) {
		Analytics.event(Analytics.Category.USER, Analytics.Action.SEND_FEEDBACK);
		Uri uri = Uri.fromParts("mailto", context.getString(R.string.feedback_email), null);
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		AppUser appUser = Application.appUser();
		Long userId = appUser.getUserPhone();
		intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.feedback_email_subject) +
											  " [" + DeviceUtils.getApplicationVersionString() +
											  "-" + Build.VERSION.SDK_INT + "-" + Build.MODEL +
											  "-" + Build.DEVICE + " - " + userId + "]");
		intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.feedback_email_body));
		context.startActivity(Intent.createChooser(intent,
												   context.getString(R.string.send_feedback)));
	}

	public static void rateApp(Context context) {
		Analytics.event(Analytics.Category.USER, Analytics.Action.REVIEW_APP);
		String packageName = context.getApplicationContext().getPackageName();
		Uri marketUri = Uri.parse(context.getString(R.string.rate_app_market_url, packageName));
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, marketUri);
		try {
			ApplicationInfo info =
			  context.getPackageManager().getApplicationInfo(Constants.MARKET_PACKAGE_ID, 0);
			if (info.packageName.equals(Constants.MARKET_PACKAGE_ID)) {
				goToMarket.setPackage(Constants.MARKET_PACKAGE_ID);
			}
		} catch (PackageManager.NameNotFoundException e) {
			// market do no exists, try another one and them the web
		}
		try {
			context.startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			Uri webUri = Uri.parse(context.getString(R.string.rate_app_web_url, packageName));
			context.startActivity(new Intent(Intent.ACTION_VIEW, webUri));
		}
	}

	public static void shareApp(Context context) {
		Analytics.event(Analytics.Category.USER, Analytics.Action.SHARED_APP);
		String packageName = context.getApplicationContext().getPackageName();
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT,
							context.getString(R.string.rate_app_web_url, packageName));
		sendIntent.setType("text/plain");
		context.startActivity(sendIntent);
	}

}
