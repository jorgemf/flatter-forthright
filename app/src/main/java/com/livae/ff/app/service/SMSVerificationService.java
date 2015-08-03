package com.livae.ff.app.service;

import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.Application;
import com.livae.ff.app.utils.PhoneVerification;

import java.util.concurrent.TimeUnit;

public class SMSVerificationService extends IntentService {

	public static final String INTENT_SMS_VERIFIED = "com.livae.ff.app.SMS_VERIFIED";

	public static final String EXTRA_CODE = "EXTRA_CODE";

	private static final int SMS_RECEIVED = 1;

	private static final int SMS_GENERIC_ERROR = 2;

	private static final String EXTRA_INTENT_TYPE = "EXTRA_INTENT_TYPE";

	private static final String LOG_TAG = "NUMBER_VERIFICATION";

	private static final String CODE_LIMIT = "#";

	public SMSVerificationService() {
		super("SMSVerificationService");
	}

	public static boolean checkSmsConfirmation(Context context) {
		boolean validated = false;
		Cursor cursor;
		final ContentResolver contentResolver = context.getContentResolver();
		Uri uri;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			uri = Telephony.Sms.CONTENT_URI;
		} else {
			uri = Uri.parse("content://sms");
		}
		String columnDate;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			columnDate = Telephony.Sms.DATE;
		} else {
			columnDate = "date";
		}
		String columnType;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			columnType = Telephony.Sms.TYPE;
		} else {
			columnType = "type";
		}
		String typeInbox;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			typeInbox = Integer.toString(Telephony.Sms.MESSAGE_TYPE_INBOX);
		} else {
			typeInbox = "1";
		}
		String oldestDate = Long.toString(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
		cursor = contentResolver.query(uri, null, columnDate + ">? AND " + columnType + "=?",
									   new String[]{oldestDate, typeInbox}, null);
		if (cursor.moveToFirst()) {
			String columnBody;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				columnBody = Telephony.Sms.BODY;
			} else {
				columnBody = "body";
			}
			String columnPhone;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				columnPhone = Telephony.Sms.ADDRESS;
			} else {
				columnPhone = "address";
			}
			int counter = 0;
			do {
				String message = cursor.getString(cursor.getColumnIndex(columnBody));
				String phoneNumber = cursor.getString(cursor.getColumnIndex(columnPhone));
				validated = checkSmsConfirmation(context, phoneNumber, message);
				counter++;
			} while (cursor.moveToNext() && counter <= 10 && !validated);
		}
		cursor.close();
		return validated;
	}

	private static boolean alternativeVerifyNumber(Intent intent, TelephonyManager tel)
	  throws NumberParseException {
		String phone = "+" + Application.appUser().getUserPhone().toString();
		String carrier = tel.getNetworkOperator() + "_" + tel.getNetworkOperatorName();
		Bundle bundle = intent.getExtras();
		String simCountry = tel.getSimCountryIso().toUpperCase();
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(phone, null);
		int phonePrefix = phoneNumber.getCountryCode();
		String bundleKeySet = "[";
		if (bundle != null) {
			for (String key : bundle.keySet()) {
				Object value = bundle.get(key);
				if (value != null) {
					bundleKeySet += key + "=" + value.toString();
				}
			}
		}
		String phoneInfo = carrier + ":" + simCountry + ":" + phonePrefix;
		boolean verified = false;
		if (bundle != null) {
			Integer errorCode = null;
			Boolean lastSendMsg = null;
			if (bundle.containsKey("errorCode")) {
				errorCode = bundle.getInt("errorCode", -1);
			}
			if (bundle.containsKey("LastSendMsg")) {
				lastSendMsg = bundle.getBoolean("LastSendMsg", false);
			}
			switch (phonePrefix) {
				case 593: // ecuador
					// Telefónica movistar ecuador does not send sms to the own numbers
					verified = tel.getNetworkOperator().equals("00") && simCountry.equals("EC") &&
							   errorCode != null && errorCode == 0 && lastSendMsg != null &&
							   lastSendMsg;
					break;
			}
		}
		Analytics.event(Analytics.Category.SMS_VERIFICATION_ERROR, phoneInfo,
						bundleKeySet + "=" + verified);
		return verified;
	}

	public static boolean checkSmsConfirmation(Context context, String phoneNumber,
											   String message) {
		boolean valid;
		PhoneVerification phoneVerification = PhoneVerification.instance(context);
		String codeString =
		  CODE_LIMIT + phoneVerification.getVerificationToken().toString() + CODE_LIMIT;
		String phoneToVerify = "+" + phoneVerification.getUserPhone();
		valid = message != null && phoneNumber != null && message.contains(codeString) &&
				phoneNumber.equals(phoneToVerify);
		Log.i(LOG_TAG, "VERIFY SMS: " + phoneNumber + " body: " + message + " result: " + valid);
		return valid;
	}

	private static boolean verifySMSGenericError(Context context, Intent intent) {
		// check network
		TelephonyManager tel;
		tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		boolean network = !TextUtils.isEmpty(tel.getNetworkOperatorName()) ||
						  tel.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN;

		try {
			return network && alternativeVerifyNumber(intent, tel);
		} catch (NumberParseException e) {
			Analytics.logAndReport(e, false);
		}
		return false;
	}

	private static boolean verifyIncomingSMS(Context context, Intent intent) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
			for (SmsMessage currentMessage : messages) {
				String phoneNumber = currentMessage.getOriginatingAddress();
				String message = currentMessage.getDisplayMessageBody();
				if (checkSmsConfirmation(context, phoneNumber, message)) {
					return true;
				}
			}
		} else {
			final Bundle bundle = intent.getExtras();
			if (bundle != null) {
				try {
					final Object[] pdusObj = (Object[]) bundle.get("pdus");
					for (Object aPdusObj : pdusObj) {
						SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
						String phoneNumber = currentMessage.getOriginatingAddress();
						String message = currentMessage.getDisplayMessageBody();
						if (checkSmsConfirmation(context, phoneNumber, message)) {
							return true;
						}
					}
				} catch (Exception e) {
					Analytics.logAndReport(e);
				}
			}
		}
		return false;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.hasExtra(EXTRA_INTENT_TYPE)) {
			switch (intent.getIntExtra(EXTRA_INTENT_TYPE, -1)) {
				case SMS_RECEIVED:
					if (verifyIncomingSMS(this, intent)) {
						sendSmsVerifiedBroadcast();
					}
					break;
				case SMS_GENERIC_ERROR:
					if (verifySMSGenericError(this, intent)) {
						sendSmsVerifiedBroadcast();
					}
					break;
			}
		}
	}

	private void sendSmsVerifiedBroadcast() {
		Intent intentVerified = new Intent(INTENT_SMS_VERIFIED);
		PhoneVerification phoneVerification = PhoneVerification.instance(this);
		intentVerified.putExtra(EXTRA_CODE, phoneVerification.getVerificationToken());
		sendBroadcast(intentVerified);
	}

	public static class IncomingSms extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "Received sms");
			Intent serviceIntent = new Intent(context, SMSVerificationService.class);
			serviceIntent.putExtras(intent.getExtras());
			serviceIntent.putExtra(EXTRA_INTENT_TYPE, SMS_RECEIVED);
			context.startService(serviceIntent);
		}
	}

	public static class SmsSendBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			switch (getResultCode()) {
				case Activity.RESULT_OK:
					Log.i(LOG_TAG, "SMS sent");
					Analytics.event(Analytics.Category.SMS, Analytics.Action.SMS_SENT);
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Log.e(LOG_TAG, "Generic failure");
					Analytics.event(Analytics.Category.SMS, Analytics.Action.SMS_ERROR_GENERIC);
					Intent serviceIntent = new Intent(context, SMSVerificationService.class);
					serviceIntent.putExtras(intent.getExtras());
					serviceIntent.putExtra(EXTRA_INTENT_TYPE, SMS_GENERIC_ERROR);
					context.startService(serviceIntent);
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Log.e(LOG_TAG, "No service");
					Analytics.event(Analytics.Category.SMS, Analytics.Action.SMS_ERROR_NO_SERVICE);
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Log.e(LOG_TAG, "PDU NULL");
					Analytics.event(Analytics.Category.SMS, Analytics.Action.SMS_ERROR_PDU_NULL);
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Log.e(LOG_TAG, "Radio off");
					Analytics.event(Analytics.Category.SMS, Analytics.Action.SMS_ERROR_RADIO_OFF);
					break;
			}
		}
	}

	public static class SmsDeliveredBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			switch (getResultCode()) {
				case Activity.RESULT_OK:
					Log.i(LOG_TAG, "SMS delivered");
					Analytics.event(Analytics.Category.SMS, Analytics.Action.SMS_DELIVERED);
					break;
				case Activity.RESULT_CANCELED:
					Log.i(LOG_TAG, "SMS not delivered");
					Analytics.event(Analytics.Category.SMS, Analytics.Action.SMS_NOT_DELIVERED);
					break;
			}
		}
	}
}
