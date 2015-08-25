package com.livae.ff.api.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
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
		if (limit != null) {
			limit = (int) Math.round((double) limit / Settings.LIST_STEP) * Settings.LIST_STEP;
		}
		if (limit == null) {
			limit = Settings.DEFAULT_LIST_LIMIT;
		} else if (limit < Settings.MIN_LIST_LIMIT) {
			limit = Settings.MIN_LIST_LIMIT;
		} else if (limit > Settings.MAX_LIST_LIMIT) {
			limit = Settings.MAX_LIST_LIMIT;
		}
		return limit;
	}

	public static boolean isValidNumber(Long number) {
		try {
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			Phonenumber.PhoneNumber phoneNumber = null;
			phoneNumber = phoneUtil.parse("+" + number.toString(), null);
			PhoneNumberUtil.PhoneNumberType numberType = phoneUtil.getNumberType(phoneNumber);
			return phoneUtil.isPossibleNumber(phoneNumber) &&
				   phoneUtil.isValidNumber(phoneNumber) &&
				   (numberType == PhoneNumberUtil.PhoneNumberType.MOBILE ||
					numberType == PhoneNumberUtil.PhoneNumberType.UNKNOWN ||
					numberType == PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE);
		} catch (NumberParseException e) {
			return false;
		}
	}

}
