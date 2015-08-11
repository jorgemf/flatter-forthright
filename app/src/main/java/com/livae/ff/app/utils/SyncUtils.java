package com.livae.ff.app.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.AppUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.Constants;
import com.livae.ff.app.provider.ContactsProvider;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.settings.Chats;

import java.util.concurrent.TimeUnit;

public class SyncUtils {

	private static final String LOG_TAG = "SYNC_UTILS";

	private static final String USER_DATA_PHONE = "user.phone";

	public static void createAccount(Context context, Long phone) {
		final String name = phone.toString() + Constants.ACCOUNT_SUFFIX;
		Account account = new Account(name, Constants.ACCOUNT_TYPE);
		AccountManager accountManager;
		accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
		if (accountManager.addAccountExplicitly(account, null, null)) {
			Log.i(LOG_TAG, "Account created: " + name);
			syncContactsNow();
			syncContactsWhenChange();
			syncCommentsWhenNetwork();
//			syncContactsEveryDay();
			accountManager.setUserData(account, USER_DATA_PHONE, phone.toString());
		} else {
			Log.w(LOG_TAG, "Could not create the account, maybe it exists before.");
			try {
				syncContactsNow();
				syncContactsWhenChange();
				syncCommentsWhenNetwork();
			} catch (Exception e) {
				Analytics.logAndReport(e, false);
			}
			try {
				accountManager.setUserData(account, USER_DATA_PHONE, phone.toString());
			} catch (IllegalArgumentException e) {
				Analytics.logAndReport(e, false);
			}
		}
	}

	public static void syncContactsWhenChange() {
		ContentObserver contentObserver = new ContentObserver(null) {

			@Override
			public void onChange(boolean selfChange) {
				onChange(selfChange, null);
			}

			@Override
			public void onChange(boolean selfChange, Uri changeUri) {
				final Long phone = Application.appUser().getUserPhone();
				if (phone != null) {
					final String name = phone.toString() + Constants.ACCOUNT_SUFFIX;
					final Account account = new Account(name, Constants.ACCOUNT_TYPE);
					final String authority = ContactsProvider.getAuthority(ContactsProvider.class);
					ContentResolver.requestSync(account, authority, Bundle.EMPTY);
				}
			}
		};
		final Uri uri = ContactsContract.Contacts.CONTENT_URI;
//		ContactsContract.Contacts.CONTENT_LOOKUP_URI;
		ContentResolver contentResolver = Application.getContext().getContentResolver();
		contentResolver.registerContentObserver(uri, true, contentObserver);
	}

	public static void syncContactsEveryDay() {
		final Long phone = Application.appUser().getUserPhone();
		if (phone != null) {
			final String name = phone.toString() + Constants.ACCOUNT_SUFFIX;
			final Account account = new Account(name, Constants.ACCOUNT_TYPE);
			final String authority = ContactsProvider.getAuthority(ContactsProvider.class);
			ContentResolver.addPeriodicSync(account, authority, Bundle.EMPTY,
											TimeUnit.DAYS.toSeconds(1));
		}
	}

	public static void syncContactsNow() {
		final Long phone = Application.appUser().getUserPhone();
		if (phone != null) {
			final String name = phone.toString() + Constants.ACCOUNT_SUFFIX;
			final Account account = new Account(name, Constants.ACCOUNT_TYPE);
			final String authority = ContactsProvider.getAuthority(ContactsProvider.class);
			Bundle settingsBundle = new Bundle();
			settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
			ContentResolver.requestSync(account, authority, settingsBundle);
		}
	}

	public static void syncConversationsNow() {
		final Long phone = Application.appUser().getUserPhone();
		if (phone != null) {
			final String name = phone.toString() + Constants.ACCOUNT_SUFFIX;
			final Account account = new Account(name, Constants.ACCOUNT_TYPE);
			final String authority = ContactsProvider.getAuthority(ConversationsProvider.class);
			Bundle settingsBundle = new Bundle();
			settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
			ContentResolver.requestSync(account, authority, settingsBundle);
		}
	}

	public static boolean isAccountRegistered(Context context) {
		final AppUser appUser = Application.appUser();
		final Long phone = appUser.getUserPhone();
		boolean registered = false;
		if (phone != null) {
			final String name = phone.toString() + Constants.ACCOUNT_SUFFIX;
			final Account account = new Account(name, Constants.ACCOUNT_TYPE);
			AccountManager accountManager;
			accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
			registered = accountManager.getUserData(account, USER_DATA_PHONE) != null;
		}
		if (!registered) {
			appUser.setUserPhone(null);
			appUser.setAccessToken(null);
		}
		return registered;
	}

	public static void syncUserProfile(Context context) {
		if (!BuildConfig.TEST) {
			final String[] projection =
			  {ContactsContract.Profile.PHOTO_THUMBNAIL_URI, ContactsContract.Profile
															   .DISPLAY_NAME};
			Cursor cursor = context.getContentResolver()
								   .query(ContactsContract.Profile.CONTENT_URI, projection, null,
										  null, null);
			String imageUri = null;
			String userName = null;
			if (cursor.moveToFirst()) {
				imageUri =
				  cursor.getString(cursor.getColumnIndex(ContactsContract.Profile
														   .PHOTO_THUMBNAIL_URI));
				userName =
				  cursor.getString(cursor.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));

			}
			cursor.close();
			Chats chats = Application.appUser().getChats();
			chats.setUserImageUri(imageUri);
			chats.setUserDisplayName(userName);
		}
	}

	public static void syncCommentsWhenNetwork() {
		final Long phone = Application.appUser().getUserPhone();
		if (phone != null) {
			final String name = phone.toString() + Constants.ACCOUNT_SUFFIX;
			final Account account = new Account(name, Constants.ACCOUNT_TYPE);
			final String authority = ContactsProvider.getAuthority(ConversationsProvider.class);
			ContentResolver.setSyncAutomatically(account, authority, true);
		}
	}

}
