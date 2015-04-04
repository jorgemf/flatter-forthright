package com.livae.ff.app.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.sql.DBHelper;

public abstract class AbstractProvider extends ContentProvider {

	protected static final String CONTENT_URI_BASE = "content://";

	protected static final String TYPE_LIST_BASE = "vnd.android.cursor.dir/vnd.ff.livae.";

	protected static final String TYPE_ITEM_BASE = "vnd.android.cursor.item/vnd.ff.livae.";

	protected UriMatcher uriMatcher;

	private SQLiteDatabase dbRead;

	private SQLiteDatabase dbWrite;

	private DBHelper dbHelper;

	public static String getAuthority(Class objectClass) {
		String authority = objectClass.getCanonicalName();
		if (BuildConfig.DEV) {
			authority = authority.replace("com.livae.ff.app.", "com.livae.ff.app.dev.");
		}
		return authority;
	}

	@Override
	public boolean onCreate() {
		dbHelper = DBHelper.instance(getContext());
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		return true;
	}

	@Override
	public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		int insertedOrUpdated = super.bulkInsert(uri, values);
		db.setTransactionSuccessful();
		db.endTransaction();
		return insertedOrUpdated;
	}

	protected SQLiteDatabase getReadableDatabase() {
		if (dbRead == null || !dbRead.isOpen()) {
			dbRead = dbHelper.getReadableDatabase();
		}
		return dbRead;
	}

	protected SQLiteDatabase getWritableDatabase() {
		if (dbWrite == null || !dbWrite.isOpen()) {
			dbWrite = dbHelper.getWritableDatabase();
		}
		return dbWrite;
	}

}