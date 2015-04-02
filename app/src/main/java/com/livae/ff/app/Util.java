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
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur
											  .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (Integer.parseInt(cur.getString(cur
													 .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) >
					0) {
					Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
										   ContactsContract.CommonDataKinds.Phone.CONTACT_ID +
										   " = ?", new String[]{id}, null);
					while (pCur.moveToNext()) {
						String phoneNo = pCur.getString(pCur
														  .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					}
					pCur.close();
				}
			}
		}
		cur.close();
	}

}
