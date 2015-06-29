package com.livae.ff.app.utils;

import android.content.Context;

import com.livae.ff.app.R;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class UnitUtils {

	private static final DateFormat DATE_FORMAT_THIS_YEAR = new SimpleDateFormat("MMM d");

	private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);

	private static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT);

	static {
		TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		TIME_FORMAT.setTimeZone(TimeZone.getDefault());
	}

	public static CharSequence getAgoTime(Context context, final long time) {
		long now = System.currentTimeMillis();
		long passTime = now - time;
		String timeText;
		if (passTime < TimeUnit.MINUTES.toMillis(1)) {
			// less than one minute ago
			timeText = context.getString(R.string.time_now);
		} else if (passTime <= TimeUnit.HOURS.toMillis(1)) {
			// less than one hour ago
			int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(passTime);
			timeText = context.getString(R.string.time_few_minutes_ago, minutes);
		} else {
			Calendar calendarNow = Calendar.getInstance();
			calendarNow.setTimeInMillis(now);

			calendarNow.set(Calendar.HOUR_OF_DAY, 0);
			calendarNow.set(Calendar.MINUTE, 0);
			calendarNow.set(Calendar.SECOND, 0);
			calendarNow.set(Calendar.MILLISECOND, 0);
			long todayTime = calendarNow.getTimeInMillis();
			calendarNow.set(Calendar.DAY_OF_YEAR, 1);
			long thisYearTime = calendarNow.getTimeInMillis();
			if (time > todayTime) {
				// today
				timeText = TIME_FORMAT.format(time);
			} else if (time > todayTime - TimeUnit.DAYS.toMillis(1)) {
				// yesterday
				timeText = context.getString(R.string.time_yesterday);
			} else if (time > thisYearTime) {
				// this year
				timeText = DATE_FORMAT_THIS_YEAR.format(time);
			} else {
				// other
				timeText = DATE_FORMAT.format(time);
			}
		}
		return timeText;
	}

}
