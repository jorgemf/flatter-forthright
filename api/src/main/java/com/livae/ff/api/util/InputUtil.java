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
		if (limit == null || limit < Settings.MIN_LIST_LIMIT || limit > Settings.MAX_LIST_LIMIT) {
			limit = Settings.DEFAULT_LIST_LIMIT;
		}
		return limit;
	}

	public static Long getValidNumber(String number, String countryCode) {
		try {
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			Phonenumber.PhoneNumber phoneNumber = null;
			phoneNumber = phoneUtil.parseAndKeepRawInput(number, countryCode);
			PhoneNumberUtil.PhoneNumberType numberType = phoneUtil.getNumberType(phoneNumber);
			if (phoneUtil.isValidNumber(phoneNumber) &&
				numberType == PhoneNumberUtil.PhoneNumberType.MOBILE) {
				return Long.parseLong(Integer.toString(phoneNumber.getCountryCode()) +
									  Long.toString(phoneNumber.getNationalNumber()));
			} else {
				return null;
			}
		} catch (NumberParseException e) {
			return null;
		}
	}

	public static boolean isValidNumber(Long number) {
		try {
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			Phonenumber.PhoneNumber phoneNumber = null;
			phoneNumber = phoneUtil.parseAndKeepRawInput("+" + Long.toString(number), null);
			PhoneNumberUtil.PhoneNumberType numberType = phoneUtil.getNumberType(phoneNumber);
			return phoneUtil.isValidNumber(phoneNumber) &&
				   numberType == PhoneNumberUtil.PhoneNumberType.MOBILE;
		} catch (NumberParseException e) {
			return false;
		}
	}
}
