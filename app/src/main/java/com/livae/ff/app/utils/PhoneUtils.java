package com.livae.ff.app.utils;

import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.livae.ff.app.BuildConfig;

public class PhoneUtils {

	private static final String LOG_TAG = "PHONE_UTILS";

	public static Long getMobileNumber(String phone, String countryISO) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		Phonenumber.PhoneNumber phoneNumber = getPhoneNumber(phone, countryISO);
		if (phoneNumber == null) {
			return null;
		}
		PhoneNumberUtil.PhoneNumberType numberType = phoneUtil.getNumberType(phoneNumber);
		if (phoneUtil.isPossibleNumber(phoneNumber) &&
			phoneUtil.isValidNumber(phoneNumber) &&
			(numberType == PhoneNumberUtil.PhoneNumberType.MOBILE ||
			 numberType == PhoneNumberUtil.PhoneNumberType.UNKNOWN)) {
			String phoneString =
			  "+" + phoneNumber.getCountryCode() + phoneNumber.getNationalNumber();
			return Long.parseLong(phoneString);
		} else //noinspection PointlessBooleanExpression,ConstantConditions
			if (BuildConfig.DEV && phoneNumber.getCountryCode() == 1 &&
				phoneNumber.getNationalNumber() == 5555215554L) {
				return 15555215554L; // emulator phone number
			} else {
				return null;
			}
	}

	private static Phonenumber.PhoneNumber getPhoneNumber(String phone, String countryISO) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		Phonenumber.PhoneNumber phoneNumber;
		try {
			if (phone.startsWith("+")) {
				phoneNumber = phoneUtil.parse(phone, null);
			} else {
				phoneNumber = phoneUtil.parse(phone, "");
			}
		} catch (NumberParseException e) {
			if (e.getErrorType() == NumberParseException.ErrorType.INVALID_COUNTRY_CODE &&
				countryISO != null) {
				try {
					phoneNumber = phoneUtil.parse(phone, countryISO);
				} catch (NumberParseException error) {
					Log.e(LOG_TAG, "Number: " + phone + "  Region: " + countryISO);
					error.printStackTrace();
					return null;
				}
			} else {
				Log.e(LOG_TAG, "Number: " + phone);
				e.printStackTrace();
				return null;
			}
		}
		return phoneNumber;
	}

	public static String getPrettyPrint(Long phone, String countryISO) {
		if (phone == null) {
			return "";
		}
		Phonenumber.PhoneNumber phoneNumber = getPhoneNumber("+" + phone.toString(), null);
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		return phoneUtil.formatOutOfCountryCallingNumber(phoneNumber, countryISO);
	}
}
