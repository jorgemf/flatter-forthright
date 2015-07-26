package com.livae.ff.app.utils;

import android.content.Context;

import com.livae.ff.app.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class UnitUtils {

	private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);

	private static final DateFormat TIME_FORMAT = DateFormat.getTimeInstance(DateFormat.SHORT);

	public static CharSequence getTime(Context context, final long time) {
//		TIME_FORMAT.setTimeZone(TimeZone.getDefault());
		return TIME_FORMAT.format(time);
	}

	public static CharSequence getDate(Context context, final long time) {
//		final TimeZone timezone = TimeZone.getDefault();
//		DATE_FORMAT.setTimeZone(timezone);
		Calendar calendarNow = Calendar.getInstance();
//		calendarNow.setTimeZone(timezone);
		calendarNow.setTimeInMillis(time);
		calendarNow.set(Calendar.HOUR_OF_DAY, 0);
		calendarNow.set(Calendar.MINUTE, 0);
		calendarNow.set(Calendar.SECOND, 0);
		calendarNow.set(Calendar.MILLISECOND, 0);
		long todayTime = calendarNow.getTimeInMillis();
		calendarNow.set(Calendar.DAY_OF_YEAR, 1);
		long thisYearTime = calendarNow.getTimeInMillis();
		String timeText;
		if (time > todayTime) {
			// today
			timeText = context.getString(R.string.time_today);
		} else if (time > todayTime - TimeUnit.DAYS.toMillis(1)) {
			// yesterday
			timeText = context.getString(R.string.time_yesterday);
		} else if (time > thisYearTime) {
			// this year
			timeText = DATE_FORMAT.format(time);
		} else {
			// any other year
			timeText = DATE_FORMAT.format(time);
		}
		return timeText;
	}

	public static boolean isItSameDay(final long timeA, final long timeB) {
		if (Math.abs(timeA - timeB) < TimeUnit.DAYS.toMillis(1)) {
//			final TimeZone timezone = TimeZone.getDefault();
			Calendar calendarA = Calendar.getInstance();
//			calendarA.setTimeZone(timezone);
			calendarA.setTimeInMillis(timeA);
			calendarA.set(Calendar.HOUR_OF_DAY, 0);
			calendarA.set(Calendar.MINUTE, 0);
			calendarA.set(Calendar.SECOND, 0);
			calendarA.set(Calendar.MILLISECOND, 0);
			Calendar calendarB = Calendar.getInstance();
//			calendarB.setTimeZone(timezone);
			calendarB.setTimeInMillis(timeA);
			calendarB.set(Calendar.HOUR_OF_DAY, 0);
			calendarB.set(Calendar.MINUTE, 0);
			calendarB.set(Calendar.SECOND, 0);
			calendarB.set(Calendar.MILLISECOND, 0);
			return calendarA.getTimeInMillis() == calendarB.getTimeInMillis();
		} else {
			return false;
		}
	}

}
