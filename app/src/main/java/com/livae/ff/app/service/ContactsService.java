package com.livae.ff.app.service;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.util.Pair;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.provider.DataProvider;
import com.livae.ff.app.sql.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactsService extends IntentService {

	private static final String LOG_TAG = "CONTACTS_SERVICE";

	public ContactsService() {
		super("ContactsService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		List<Long> phonesToDelete = new ArrayList<>();
		List<Pair<String, Long>> phonesToAdd = new ArrayList<>();
		List<Pair<String, Long>> phonesToUpdate = new ArrayList<>();

		List<Pair<String, Long>> phoneContacts = getPhoneContacts();
		ContentResolver contentResolver = getContentResolver();
		Cursor appContacts = contentResolver.query(DataProvider.getUriContacts(), null, null, null,
												   Table.LocalUser.PHONE);
		if (appContacts.moveToFirst()) {
			int indexPhoneContacts = 0;
			int iNumber = appContacts.getColumnIndex(Table.LocalUser.PHONE);
			int iName = appContacts.getColumnIndex(Table.LocalUser.CONTACT);
			int iBlocked = appContacts.getColumnIndex(Table.LocalUser.BLOCKED);
			do {
				String currentName = appContacts.getString(iName);
				long currentNumber = appContacts.getLong(iNumber);
				if (indexPhoneContacts >= phoneContacts.size()) {
					// deleted a phone that is not in the mobile anymore, but not blocked users
					if (appContacts.isNull(iBlocked) || appContacts.getInt(iBlocked) == 0) {
						phonesToDelete.add(currentNumber);
					}
				} else if (indexPhoneContacts < phoneContacts.size()) {
					// add or update the phones
					Pair<String, Long> phoneContact = phoneContacts.get(indexPhoneContacts);
					do {
						if (phoneContact.second == currentNumber) {
							if (!currentName.equals(phoneContact.first)) {
								phonesToUpdate.add(new Pair<>(phoneContact.first, currentNumber));
							}
							indexPhoneContacts++;
						} else if (phoneContact.second < currentNumber) {
							phonesToAdd.add(phoneContact);
							indexPhoneContacts++;
						}
					} while (phoneContact.second <= currentNumber &&
							 indexPhoneContacts < phoneContacts.size());
				}
			} while (appContacts.moveToNext());
			while (indexPhoneContacts < phoneContacts.size()) {
				phonesToDelete.add(phoneContacts.get(indexPhoneContacts).second);
				indexPhoneContacts++;
			}
		} else {
			// data base empty
			for (Pair<String, Long> userPhone : phoneContacts) {
				phonesToAdd.add(userPhone);
			}
		}
		appContacts.close();
		ArrayList<ContentProviderOperation> operations = new ArrayList<>();
		deletePhones(operations, phonesToDelete);
		addPhones(operations, phonesToAdd);
		updatePhones(operations, phonesToUpdate);
		try {
			getContentResolver().applyBatch(DataProvider.getAuthority(DataProvider.class),
											operations);
		} catch (RemoteException e) {
			Analytics.logAndReport(e, false);
		} catch (OperationApplicationException e) {
			Analytics.logAndReport(e, false);
		}
	}

	private void deletePhones(ArrayList<ContentProviderOperation> operations,
							  List<Long> phonesToDelete) {
		ContentProviderOperation operation;
		for (Long phone : phonesToDelete) {
			operation = ContentProviderOperation.newDelete(DataProvider.getUriContact(phone))
												.withExpectedCount(1).build();
			operations.add(operation);
		}
//		final DBHelper dbHelper = DBHelper.instance(this);
//		final SQLiteDatabase database = dbHelper.getWritableDatabase();
//		database.beginTransaction();
//		String[] args = new String[1];
//		for (Long phone : phonesToDelete) {
//			args[0] = phone.toString();
//			int deleted = database.delete(Table.LocalUser.NAME, Table.LocalUser.PHONE + "=?", args);
//			if (deleted != 1) {
//				Analytics.logAndReport("Should delete 1 but it deleted " + deleted +
//									   "  (Phone number: " + args[0] + ")");
//			} else
//				//noinspection ConstantConditions,PointlessBooleanExpression
//				if (BuildConfig.DEV && BuildConfig.DEBUG) {
//					Log.d(LOG_TAG, "Deleted: " + args[0]);
//				}
//		}
//		database.endTransaction();
	}

	private void addPhones(ArrayList<ContentProviderOperation> operations,
						   List<Pair<String, Long>> phonesToAdd) {
		ContentProviderOperation operation;
		for (Pair<String, Long> pair : phonesToAdd) {
			operation = ContentProviderOperation.newInsert(DataProvider.getUriContacts())
												.withValue(Table.LocalUser.PHONE, pair.second)
												.withValue(Table.LocalUser.CONTACT, pair.first)
												.build();
			operations.add(operation);
		}
		/*final DBHelper dbHelper = DBHelper.instance(this);
		final SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction();
		ContentValues contentValues = new ContentValues();
		for (Pair<String, Long> pair : phonesToAdd) {
			contentValues.put(Table.LocalUser.PHONE, pair.second);
			contentValues.put(Table.LocalUser.CONTACT, pair.first);
			database.insert(Table.LocalUser.NAME, null, contentValues);
			//noinspection ConstantConditions,PointlessBooleanExpression
			if (BuildConfig.DEV && BuildConfig.DEBUG) {
				Log.d(LOG_TAG, "Added: " + pair.first + " - " + pair.second);
			}
		}
		database.endTransaction();*/
	}

	private void updatePhones(ArrayList<ContentProviderOperation> operations,
							  List<Pair<String, Long>> phonesToUpdate) {
		ContentProviderOperation operation;
		String[] args = new String[1];
		for (Pair<String, Long> pair : phonesToUpdate) {
			args[0] = pair.second.toString();
			operation = ContentProviderOperation.newUpdate(DataProvider.getUriContact(pair.second))
												.withValue(Table.LocalUser.CONTACT, pair.first)
												.withExpectedCount(1).build();
			operations.add(operation);
		}
		/*final DBHelper dbHelper = DBHelper.instance(this);
		final SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction();
		ContentValues contentValues = new ContentValues();
		String[] args = new String[1];
		for (Pair<String, Long> pair : phonesToUpdate) {
			contentValues.put(Table.LocalUser.PHONE, pair.second);
			contentValues.put(Table.LocalUser.CONTACT, pair.first);
			args[0] = pair.second.toString();
			int updated = database.update(Table.LocalUser.NAME, contentValues,
										  Table.LocalUser.PHONE + "=?", args);
			if (updated != 1) {
				Analytics.logAndReport("Should updated 1 but it updated " + updated +
									   "  (Phone number: " + args[0] + ")");
			} else
				//noinspection ConstantConditions,PointlessBooleanExpression
				if (BuildConfig.DEV && BuildConfig.DEBUG) {
					Log.d(LOG_TAG, "Updated: " + pair.first + " - " + pair.second);
				}
		}
		database.endTransaction();*/
	}

	private List<Pair<String, Long>> getPhoneContacts() {
		List<Pair<String, Long>> contactsList = new ArrayList<>();
		ContentResolver contentResolver = getContentResolver();
		Cursor phoneContacts;
		phoneContacts = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
											  ContactsContract.Contacts.HAS_PHONE_NUMBER, null,
											  null);
		int iId = phoneContacts.getColumnIndex(ContactsContract.Contacts._ID);
		int iContactLastUpdate = phoneContacts
								   .getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP);
		int iDisplayName = phoneContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
		if (phoneContacts.moveToFirst()) {
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String countryISO = tm.getSimCountryIso().toUpperCase();
			final String[] projectionPhoneUser = {ContactsContract.CommonDataKinds.Phone.NUMBER,
												  ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP};
			do {
				String id = phoneContacts.getString(iId);
				String name = phoneContacts.getString(iDisplayName);
				Long contactLastUpdate = phoneContacts.getLong(iContactLastUpdate);
				// TODO
				Cursor phonesCursor = contentResolver
										.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
											   projectionPhoneUser,
											   ContactsContract.CommonDataKinds.Phone.CONTACT_ID +
											   " = ?", new String[]{id}, null);
				int iNumber = phonesCursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				int iNumberLastUpdate = phonesCursor
										  .getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_LAST_UPDATED_TIMESTAMP);
				if (phonesCursor.moveToFirst()) {
					do {
						String phone = phonesCursor.getString(iNumber);
						Long numberLastUpdate = phonesCursor.getLong(iNumberLastUpdate);
						// TODO
						Long mobile = getMobileNumber(phone, countryISO);
						if (mobile != null) {
							contactsList.add(new Pair<>(name, mobile));
						}
					} while (phonesCursor.moveToNext());
				}
				phonesCursor.close();
			} while (phoneContacts.moveToNext());
		}
		phoneContacts.close();
		Collections.sort(contactsList, new Comparator<Pair<String, Long>>() {
			@Override
			public int compare(Pair<String, Long> lhs, Pair<String, Long> rhs) {
				return lhs.second.compareTo(rhs.second);
			}
		});
		return contactsList;
	}

	private Long getMobileNumber(String phone, String countryISO) {
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		Phonenumber.PhoneNumber phoneNumber;
		try {
			phoneNumber = phoneUtil.parse(phone, "");
		} catch (NumberParseException e) {
			if (e.getErrorType() == NumberParseException.ErrorType.INVALID_COUNTRY_CODE) {
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
		PhoneNumberUtil.PhoneNumberType numberType = phoneUtil.getNumberType(phoneNumber);
		if (phoneUtil.isPossibleNumber(phoneNumber) &&
			phoneUtil.isValidNumber(phoneNumber) &&
			(numberType == PhoneNumberUtil.PhoneNumberType.MOBILE ||
			 numberType == PhoneNumberUtil.PhoneNumberType.UNKNOWN)) {
			String phoneString =
			  "+" + phoneNumber.getCountryCode() + phoneNumber.getNationalNumber();
			return Long.parseLong(phoneString);
		} else {
			return null;
		}
	}

	class Contact {

		public String displayName;

		public String imageUri;

		public Long lastUpdateTime;

		public Long number;
	}

}
