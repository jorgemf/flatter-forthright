package com.livae.ff.app;

import android.telephony.SmsManager;

public class Util {

	private void sendSms(String phone, String message){
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phone, null, message, null, null);
	}

}
