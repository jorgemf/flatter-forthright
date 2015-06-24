package com.livae.ff.app.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import com.livae.ff.app.Application;
import com.livae.ff.app.Constants;
import com.livae.ff.app.provider.DataProvider;

import java.util.concurrent.TimeUnit;

public class SyncUtils {

	private static final String LOG_TAG = "SYNC_UTILS";

	public static void createAccount(Context context, Long phone) {
		final String name = phone.toString() + Constants.ACCOUNT_SUFFIX;
		Account account = new Account(name, Constants.ACCOUNT_TYPE);
		AccountManager accountManager;
		accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
		if (accountManager.addAccountExplicitly(account, null, null)) {
			Log.i(LOG_TAG, "Account created: " + name);
			syncContactsWhenChange();
//			syncContactsEveryDay();
			syncContactsNow();
		} else {
			Log.w(LOG_TAG, "Could not create the account, maybe it exists before.");
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
					final String authority = DataProvider.getAuthority(DataProvider.class);
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
			final String authority = DataProvider.getAuthority(DataProvider.class);
			ContentResolver.addPeriodicSync(account, authority, Bundle.EMPTY,
											TimeUnit.DAYS.toSeconds(1));
		}
	}

	public static void syncContactsNow() {
		final Long phone = Application.appUser().getUserPhone();
		if (phone != null) {
			final String name = phone.toString() + Constants.ACCOUNT_SUFFIX;
			final Account account = new Account(name, Constants.ACCOUNT_TYPE);
			final String authority = DataProvider.getAuthority(DataProvider.class);
			Bundle settingsBundle = new Bundle();
			settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
			ContentResolver.requestSync(account, authority, settingsBundle);
		}
	}

}
