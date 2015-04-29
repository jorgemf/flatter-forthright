package com.livae.ff.app.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.Constants;
import com.livae.ff.app.R;

public class IntentUtils {

	public static void openMarket(Context context, String packageName) {
		Uri marketUri = Uri.parse(context.getString(R.string.rate_app_market_url, packageName));
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, marketUri);
		try {
			ApplicationInfo info = context.getPackageManager()
										  .getApplicationInfo(Constants.MARKET_PACKAGE_ID, 0);
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

//	public static void shareApp(Context context, long appId) {
//		Analytics.event(Analytics.Category.CONTENT, Analytics.Action.APP_SHARED);
//		Intent sendIntent = new Intent();
//		sendIntent.setAction(Intent.ACTION_SEND);
//		sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_link, appId));
//		sendIntent.setType("text/plain");
//		context.startActivity(sendIntent);
//	}

	public static void shareApp(Context context, String packageName) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.rate_app_web_url,
																 packageName));
		sendIntent.setType("text/plain");
		context.startActivity(sendIntent);
	}

	public static String getAppId(Intent intent) {
		String appId = null;
		if (intent != null) {
			switch (intent.getAction()) {
				case Intent.ACTION_VIEW:
					Uri data = intent.getData();
					if (data != null) {
						appId = data.getQueryParameter("id");
					}
					break;
				case Intent.ACTION_SEND:
					String textUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
					String findString = "?id=";
					if (textUrl.contains(findString)) {
						int start = textUrl.indexOf(findString) + findString.length();
						int end = textUrl.lastIndexOf(' ');
						if (end == -1) {
							end = textUrl.lastIndexOf('\n');
						}
						if (end == -1) {
							end = textUrl.lastIndexOf('\t');
						}
						if (end > 0) {
							appId = textUrl.substring(start, end);
						} else {
							appId = textUrl.substring(start);
						}
					}
					break;
			}
		}
		return appId;
	}
}
