package com.livae.ff.app.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

import com.livae.ff.api.ff.model.Numbers;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.Constants;
import com.livae.ff.app.api.API;
import com.livae.ff.app.provider.ContactsProvider;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.utils.PhoneUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactsSyncAdapter extends AbstractThreadedSyncAdapter {

	private static final String LOG_TAG = "CONTACTS_SYNC_SERVICE";

	private ContentResolver contentResolver;

	public ContactsSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		contentResolver = context.getContentResolver();
	}

	public ContactsSyncAdapter(Context context, boolean autoInitialize,
							   boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
		contentResolver = context.getContentResolver();
	}

	private static boolean differentStrings(String a, String b) {
		return (a == null && b == null) || (a != null && a.equals(b));
	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
							  ContentProviderClient provider, SyncResult syncResult) {
		Log.i(LOG_TAG, "Starting contacts synchronization");
		Log.i(LOG_TAG, "Synchronizing database");
		syncDataBase();
		Log.i(LOG_TAG, "Synchronizing with server");
		checkPhonesWithServer();
		Log.i(LOG_TAG, "Finished contacts synchronization");
	}

	private void syncDataBase() {
		Map<Long, PhoneContact> phoneContacts = getPhoneContacts(contentResolver);
		List<LocalContact> appContacts = getAppContacts(contentResolver);
		ArrayList<ContentProviderOperation> operations = new ArrayList<>();
		ContentProviderOperation operation;
		String countryISO = PhoneUtils.getCountryISO(getContext());
		for (LocalContact localContact : appContacts) {
			PhoneContact phoneContact = phoneContacts.get(localContact.rawId);
			if (phoneContact == null && !localContact.blocked) {
				// delete
				Uri uriDelete = ContactsProvider.getUriContact(localContact.id);
				operation = ContentProviderOperation.newDelete(uriDelete).withExpectedCount(1)
													.build();
				operations.add(operation);
				if (localContact.phone != null) {
					Log.i(LOG_TAG, "Deleted user phone: " + localContact.phone);
				}
			} else if (phoneContact != null) {
				phoneContact.flag = true;
				if (phoneContact.version > localContact.lastVersion ||
					differentStrings(phoneContact.displayName, localContact.displayName) ||
					differentStrings(phoneContact.photoUri, localContact.photoUri)) {
					// update
					Uri uriUpdate = ContactsProvider.getUriContact(localContact.id);
					ContentValues contentValues;
					contentValues = phoneContact.getContentValues(localContact, countryISO);
					operation = ContentProviderOperation.newUpdate(uriUpdate)
														.withValues(contentValues).build();
					operations.add(operation);
					if (phoneContact.phone != null) {
						Log.i(LOG_TAG, "Updated user phone: " + phoneContact.phone);
					}
					contentResolver.notifyChange(ContactsProvider.getUriContact(localContact.id),
												 null);
				}
			}
		}
		final Uri uriContacts = ContactsProvider.getUriContacts();
		for (PhoneContact phoneContact : phoneContacts.values()) {
			if (!phoneContact.flag) {
				// add
				final ContentValues contentValues = phoneContact.getContentValues(countryISO);
				contentValues.put(Table.LocalUser.ACCEPTS_PRIVATE, false);
				contentValues.put(Table.LocalUser.BLOCKED, false);
				operation = ContentProviderOperation.newInsert(uriContacts)
													.withValues(contentValues).build();
				operations.add(operation);
				if (phoneContact.phone != null) {
					Log.i(LOG_TAG, "Added user phone: " + phoneContact.phone);
				}
			}
		}
		if (operations.size() > 0) {
			try {
				contentResolver.applyBatch(ContactsProvider.getAuthority(ContactsProvider.class),
										   operations);
				getContext().getContentResolver().notifyChange(uriContacts, null, false);
			} catch (RemoteException | OperationApplicationException e) {
				e.printStackTrace();
				Analytics.logAndReport(e, false);
			}
		}
		contentResolver.notifyChange(ContactsProvider.getUriContacts(), null);
	}

	private void checkPhonesWithServer() {
		Set<Long> contacts = new HashSet<>();
		final Uri uri = ContactsProvider.getUriContacts();
		String[] projection = {Table.LocalUser.PHONE};
		String select = Table.LocalUser.IS_MOBILE_NUMBER +
						" AND NOT " + Table.LocalUser.ACCEPTS_PRIVATE +
						" AND NOT " + Table.LocalUser.BLOCKED;
		final Cursor cursor = contentResolver.query(uri, projection, select, null, null);
		int iPhone = cursor.getColumnIndex(Table.LocalUser.PHONE);
		if (cursor.moveToFirst()) {
			do {
				contacts.add(cursor.getLong(iPhone));
			} while (cursor.moveToNext());
		}
		cursor.close();

		ArrayList<ContentProviderOperation> operations = new ArrayList<>();
		ContentProviderOperation operation;
		final Uri uriContacts = ContactsProvider.getUriContacts();

		// add new users of the platform
		try {
			Numbers numbers = new Numbers();
			List<Long> list = new ArrayList<Long>();
			list.addAll(contacts);
			numbers.setNumbers(list);
			Numbers validNumbers = API.endpoint().getContacts(numbers).execute();
			if (validNumbers != null && validNumbers.size() > 0 &&
				validNumbers.getNumbers() != null) {
				final String selection = Table.LocalUser.PHONE + "=?";
				final String[] selectionArgs = new String[1];
				for (Long phone : validNumbers.getNumbers()) {
					ContentValues contentValues = new ContentValues();
					contentValues.put(Table.LocalUser.ACCEPTS_PRIVATE, true);
					selectionArgs[0] = phone.toString();
					operation = ContentProviderOperation.newUpdate(uriContacts)
														.withSelection(selection, selectionArgs)
														.withValues(contentValues).build();
					operations.add(operation);
				}
			}
			try {
				contentResolver.applyBatch(ContactsProvider.getAuthority(ContactsProvider.class),
										   operations);
			} catch (RemoteException | OperationApplicationException e) {
				Analytics.logAndReport(e, false);
			}
		} catch (IOException e) {
			Analytics.logAndReport(e, false);
		}
	}

	private Map<Long, PhoneContact> getPhoneContacts(ContentResolver contentResolver) {
		HashMap<Long, PhoneContact> contacts = new HashMap<>();
		PhoneContact contact;
		final Uri uri = Phone.CONTENT_URI;
		String[] projection = {Phone._ID, Phone.DATA_VERSION, Phone.NUMBER, Phone.DATA4,
							   Phone.DISPLAY_NAME, Phone.PHOTO_THUMBNAIL_URI};
		String where = "account_type_and_data_set !=? ";
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			projection = new String[]{Phone._ID, Phone.DATA_VERSION, Phone.NUMBER,
									  Phone.NORMALIZED_NUMBER, Phone.DISPLAY_NAME,
									  Phone.PHOTO_THUMBNAIL_URI};
			where = Phone.ACCOUNT_TYPE_AND_DATA_SET + "!=? ";
		}
		final String[] args = {Constants.ACCOUNT_TYPE};
		final Cursor cursor = contentResolver.query(uri, projection, where, args, null);

		int iId = cursor.getColumnIndex(Phone._ID);
		int iDisplayName = cursor.getColumnIndex(Phone.DISPLAY_NAME);
		int iNumber = cursor.getColumnIndex(Phone.NUMBER);
		int iNormalizedNumber = cursor.getColumnIndex(Phone.DATA4);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			iNormalizedNumber = cursor.getColumnIndex(Phone.NORMALIZED_NUMBER);
		}
		int iDataVersion = cursor.getColumnIndex(Phone.DATA_VERSION);
		int iPhotoThumbnailUri = cursor.getColumnIndex(Phone.PHOTO_THUMBNAIL_URI);
		if (cursor.moveToFirst()) {
			do {
				contact = new PhoneContact();
				contact.contactId = cursor.getLong(iId);
				contact.displayName = cursor.getString(iDisplayName);
				contact.photoUri = cursor.getString(iPhotoThumbnailUri);
				contact.version = cursor.getInt(iDataVersion);
				contact.normalizedNumber = cursor.getString(iNormalizedNumber);
				contact.number = cursor.getString(iNumber);
				contacts.put(contact.contactId, contact);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return contacts;
	}

	private List<LocalContact> getAppContacts(ContentResolver contentResolver) {
		ArrayList<LocalContact> contacts = new ArrayList<>();
		LocalContact contact;
		final Uri uri = ContactsProvider.getUriContacts();
		String[] projection = {Table.LocalUser.ID, Table.LocalUser.ANDROID_RAW_CONTACT_ID,
							   Table.LocalUser.ANDROID_RAW_CONTACT_LAST_VERSION,
							   Table.LocalUser.BLOCKED, Table.LocalUser.PHONE,
							   Table.LocalUser.CONTACT_NAME, Table.LocalUser.IMAGE_URI};
		final Cursor cursor = contentResolver.query(uri, projection, null, null, null);

		int iId = cursor.getColumnIndex(Table.LocalUser.ID);
		int iBlocked = cursor.getColumnIndex(Table.LocalUser.BLOCKED);
		int iRawContactId = cursor.getColumnIndex(Table.LocalUser.ANDROID_RAW_CONTACT_ID);
		int iLastVersion = cursor.getColumnIndex(Table.LocalUser.ANDROID_RAW_CONTACT_LAST_VERSION);
		int iPhone = cursor.getColumnIndex(Table.LocalUser.PHONE);
		int iContactName = cursor.getColumnIndex(Table.LocalUser.CONTACT_NAME);
		int iImageUri = cursor.getColumnIndex(Table.LocalUser.IMAGE_URI);
		if (cursor.moveToFirst()) {
			do {
				contact = new LocalContact();
				contact.id = cursor.getLong(iId);
				contact.blocked = !cursor.isNull(iBlocked) && cursor.getInt(iBlocked) != 0;
				if (!cursor.isNull(iRawContactId)) {
					contact.rawId = cursor.getLong(iRawContactId);
					contact.lastVersion = cursor.getInt(iLastVersion);
				}
				if (!cursor.isNull(iPhone)) {
					contact.phone = cursor.getLong(iPhone);
				}
				if (!cursor.isNull(iContactName)) {
					contact.displayName = cursor.getString(iContactName);
				}
				if (!cursor.isNull(iImageUri)) {
					contact.photoUri = cursor.getString(iImageUri);
				}
				contacts.add(contact);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return contacts;
	}

	class LocalContact {

		public long id;

		public boolean blocked;

		public Long rawId;

		public Integer lastVersion;

		public Long phone;

		public String displayName;

		public String photoUri;
	}

	class PhoneContact {

		public Long contactId;

		public String displayName;

		public String photoUri;

		public int version;

		public String normalizedNumber;

		public String number;

		public boolean flag = false;

		public Long phone;

		public ContentValues getContentValues(String countryISO) {
			return getContentValues(null, countryISO);
		}

		public ContentValues getContentValues(LocalContact localContact, String countryISO) {
			phone = getPhone(countryISO);
			ContentValues contentValues = new ContentValues();
			if (phone != null) {
				contentValues.put(Table.LocalUser.PHONE, phone);
				contentValues.put(Table.LocalUser.IS_MOBILE_NUMBER, true);
			} else {
				contentValues.putNull(Table.LocalUser.PHONE);
				contentValues.put(Table.LocalUser.IS_MOBILE_NUMBER, false);
			}
			contentValues.put(Table.LocalUser.CONTACT_NAME, displayName);
			contentValues.put(Table.LocalUser.IMAGE_URI, photoUri);
			contentValues.put(Table.LocalUser.ANDROID_RAW_CONTACT_LAST_VERSION, version);
			contentValues.put(Table.LocalUser.ANDROID_RAW_CONTACT_ID, contactId);
			if (localContact != null) {
				contentValues.put(Table.LocalUser.ID, localContact.id);
			}
			return contentValues;
		}

		private Long getPhone(String countryISO) {
			Long number = null;
			if (normalizedNumber != null) {
				number = PhoneUtils.getMobileNumber(this.normalizedNumber, countryISO);
			}
			if (number == null) {
				number = PhoneUtils.getMobileNumber(this.number, countryISO);
			}
			return number;
		}
	}
}
