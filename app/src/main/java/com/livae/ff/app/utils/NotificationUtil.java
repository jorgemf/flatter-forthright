package com.livae.ff.app.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.R;
import com.livae.ff.app.service.CloudMessagesService;
import com.livae.ff.common.Constants.PushNotificationType;
import com.livae.ff.common.model.Notification;

public class NotificationUtil {

	private static final StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);

	private static final StyleSpan italicSpan = new StyleSpan(Typeface.ITALIC);

	public static PushNotificationType getNotificationType(Bundle extras) {
		PushNotificationType notificationType = null;
		if (extras != null) {
			String type = extras.getString("t");
			try {
				notificationType = PushNotificationType.valueOf(type);
			} catch (IllegalArgumentException ignore) {
				// unknown type
			}
		}
		return notificationType;
	}

	public static NotificationCompat.Builder getDefaultNotificationBuilder(Context context) {
		Resources res = context.getResources();
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setContentTitle(res.getString(R.string.app_name));
		builder.setDefaults(android.app.Notification.DEFAULT_ALL);
		builder.setSmallIcon(R.drawable.ic_stat_notifications);
		builder.setCategory("Pensamientos");
		builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		builder.setAutoCancel(true);
		builder.setColor(res.getColor(R.color.purple));
		return builder;
	}

	public static SpannableString makeNotificationLine(String title, String text,
													   String italicText) {
		final SpannableString spannableString;
		if (title != null && title.length() > 0) {
			spannableString = new SpannableString(String.format("%s  %s%s", title, text,
																italicText));
			spannableString.setSpan(boldSpan, 0, title.length(),
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			if (italicText.length() > 0) {
				int length = spannableString.length();
				spannableString.setSpan(italicText, length - italicText.length() - 1, length - 1,
										Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		} else {
			spannableString = new SpannableString(text);
		}
		return spannableString;
	}

	public static SpannableString setBoldText(String text, String boldText) {
		final SpannableString spannableString = new SpannableString(text);
		if (boldText != null && boldText.length() > 0) {
			int pos = text.indexOf(boldText);
			if (pos >= 0) {
				spannableString.setSpan(boldSpan, pos, pos + boldText.length(),
										Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return spannableString;
	}

	public static Bitmap getLargeDefaultIcon(Context context) {
		return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
	}

	public static Notification parseNotification(Bundle extras) {
		String jsonMessage = extras.getString("m");
		String jsonType = extras.getString("t");
		Log.d(CloudMessagesService.LOG_TAG, jsonType + ": " + jsonMessage);
		PushNotificationType notificationType = getNotificationType(extras);
		if (notificationType == null) {
			return null;
		}
		Gson gson = new GsonBuilder().create();
		try {
			switch (notificationType) {
				case COMMENT:
					com.livae.ff.common.model.NotificationComment nc;
					nc = gson.fromJson(jsonMessage,
									   com.livae.ff.common.model.NotificationComment.class);
//				break;
					return nc;
				default:
					return null;
			}
		} catch (JsonSyntaxException e) {
			Analytics.logAndReport(e, false);
		}
		return null;
	}

}
