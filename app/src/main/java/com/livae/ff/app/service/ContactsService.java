package com.livae.ff.app.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.support.v4.util.Pair;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.provider.DataProvider;
import com.livae.ff.app.sql.DBHelper;
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
			do {
				String currentName = appContacts.getString(iName);
				long currentNumber = appContacts.getLong(iNumber);
				if (indexPhoneContacts >= phoneContacts.size()) {
					// deleted a phone that is not in the mobile anymore
					phonesToDelete.add(currentNumber);
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
		}
		appContacts.close();
		deletePhones(phonesToDelete);
		addPhones(phonesToAdd);
		updatePhones(phonesToUpdate);
	}

	private void deletePhones(List<Long> phonesToDelete) {
		final DBHelper dbHelper = DBHelper.instance(this);
		final SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction();
		String[] args = new String[1];
		for (Long phone : phonesToDelete) {
			args[0] = phone.toString();
			int deleted = database.delete(Table.LocalUser.NAME, Table.LocalUser.PHONE + "=?", args);
			if (deleted != 1) {
				Analytics.logAndReport("Should delete 1 but it deleted " + deleted +
									   "  (Phone number: " + args[0] + ")");
			} else
				//noinspection ConstantConditions,PointlessBooleanExpression
				if (BuildConfig.DEV && BuildConfig.DEBUG) {
					Log.d(LOG_TAG, "Deleted: " + args[0]);
				}
		}
		database.endTransaction();
	}

	private void addPhones(List<Pair<String, Long>> phonesToAdd) {
		final DBHelper dbHelper = DBHelper.instance(this);
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
		database.endTransaction();
	}

	private void updatePhones(List<Pair<String, Long>> phonesToUpdate) {
		final DBHelper dbHelper = DBHelper.instance(this);
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
		database.endTransaction();

	}

	private List<Pair<String, Long>> getPhoneContacts() {
		List<Pair<String, Long>> contactsList = new ArrayList<>();
		ContentResolver contentResolver = getContentResolver();
		Cursor phoneContacts;
		phoneContacts = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
											  ContactsContract.Contacts.HAS_PHONE_NUMBER, null,
											  null);
		int iId = phoneContacts.getColumnIndex(ContactsContract.Contacts._ID);
		int iDisplayName = phoneContacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
		if (phoneContacts.moveToFirst()) {
			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String countryISO = tm.getSimCountryIso();
			do {
				String id = phoneContacts.getString(iId);
				String name = phoneContacts.getString(iDisplayName);
				Cursor phonesCursor = contentResolver
										.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
											   null,
											   ContactsContract.CommonDataKinds.Phone.CONTACT_ID +
											   " = ?", new String[]{id}, null);
				int iNumber = phonesCursor
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
				if (phonesCursor.isBeforeFirst()) {
					do {
						String phone = phonesCursor.getString(iNumber);
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
			phoneNumber = phoneUtil.parseAndKeepRawInput(phone, countryISO);
		} catch (NumberParseException e) {
			e.printStackTrace();
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
		} else {
			return null;
		}
	}

}
