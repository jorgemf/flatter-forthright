package com.livae.ff.app.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.util.Pair;
import android.telephony.TelephonyManager;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.livae.ff.app.provider.DataProvider;
import com.livae.ff.app.sql.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactsService extends IntentService {

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
				Long currentNumber = appContacts.getLong(iNumber);
				if (indexPhoneContacts >= phoneContacts.size()) {
					phonesToAdd.add(new Pair<String, Long>(currentName, currentNumber));
				} else {

				}
			} while (appContacts.moveToNext());
			// TODO
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
