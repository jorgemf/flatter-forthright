package com.livae.ff.api.util;

import com.livae.ff.api.Settings;

public class InputUtil {

	public static boolean isEmpty(String string) {
		return string == null || string.trim().length() == 0;
	}

	public static boolean isBetweenLimits(String string, int min, int max) {
		if (string == null) {
			return false;
		}
		int length = string.length();
		return length >= min && length <= max;
	}

	public static int getLimit(Integer limit) {
		if (limit == null || limit < Settings.MIN_LIST_LIMIT || limit > Settings.MAX_LIST_LIMIT) {
			limit = Settings.DEFAULT_LIST_LIMIT;
		}
		return limit;
	}

	public static boolean isValidNumber(Long number) {
		// TODO check it is a valid phone number
		return false;
	}
}
