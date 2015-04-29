package com.livae.ff.app;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

public class Util {

	private void sendSms(String phone, String message) {
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(phone, null, message, null, null);
	}

	public void getContactsNumbers(Context context) {
		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				int iId = cur.getColumnIndex(ContactsContract.Contacts._ID);
				String id = cur.getString(iId);
				int iName = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
				String name = cur.getString(iName);
				int iHasPhone = cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
				if (Integer.parseInt(cur.getString(iHasPhone)) > 0) {
					Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
										   ContactsContract.CommonDataKinds.Phone.CONTACT_ID +
										   " = ?", new String[]{id}, null);
					int iNumber = pCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
					while (pCur.moveToNext()) {
						String phoneNo = pCur.getString(iNumber);
						// TODO
					}
					pCur.close();
				}
			}
		}
		cur.close();
	}

}
