package com.livae.ff.app.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.livae.ff.app.sql.Table;
import com.livae.ff.common.Constants.CommentType;

import java.util.List;

public class DataProvider extends AbstractProvider {

	private static final int URI_CONTACTS = 1;

	private static final int URI_PHONE_COMMENTS = 2;

	private static final int URI_COMMENTS = 3;

	private static Uri getContentUri() {
		return Uri.parse(CONTENT_URI_BASE + getAuthority(DataProvider.class));
	}

	public static Uri getUriPhoneComments(Long phone, CommentType commentType) {
		return Uri.withAppendedPath(getContentUri(),
									Table.Comment.NAME + "/" + phone + "/" + commentType);
	}

	public static Uri getUriComments() {
		return Uri.withAppendedPath(getContentUri(), Table.Comment.NAME);
	}

	public static Uri getUriContacts() {
		return Uri.withAppendedPath(getContentUri(), Table.LocalUser.NAME);
	}

	@Override
	public boolean onCreate() {
		final boolean result = super.onCreate();
		final String authority = getAuthority(this.getClass());
		uriMatcher.addURI(authority, Table.LocalUser.NAME, URI_CONTACTS);
		uriMatcher.addURI(authority, Table.Comment.NAME, URI_COMMENTS);
		uriMatcher.addURI(authority, Table.Comment.NAME + "/#/*/", URI_PHONE_COMMENTS);
		return result;
	}

	@Override
	public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
		final int uriId = uriMatcher.match(uri);
		final int numValues = values.length;
		String query;
		String table;
		String[] args = new String[1];
		final SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		switch (uriId) {
			case URI_CONTACTS:
				table = Table.LocalUser.NAME;
				query = Table.LocalUser.ID + "=?";
				for (ContentValues value : values) {
					args[0] = value.getAsString(Table.LocalUser.ID);
					if (db.update(table, value, query, args) == 0) {
						db.insert(table, null, value);
					}
				}
				break;
			case URI_COMMENTS:
				table = Table.Comment.NAME;
				query = Table.Comment.ID + "=?";
				for (ContentValues value : values) {
					args[0] = value.getAsString(Table.Comment.ID);
					if (db.update(table, value, query, args) == 0) {
						db.insert(table, null, value);
					}
				}
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		return numValues;
	}

	@Override
	public Cursor query(Uri uri, String[] select, String where, String[] args, String order) {
		Cursor c;
		final int uriId = uriMatcher.match(uri);
		final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		List<String> pathSegments = uri.getPathSegments();
		switch (uriId) {
			case URI_CONTACTS:
				qb.setTables(Table.LocalUser.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
//			case URI_COMMENTS:
//				qb.setTables(Table.Comment.NAME);
//				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
//				break;
			case URI_PHONE_COMMENTS:
				where = Table.Comment.USER_ID + "=? AND " + Table.Comment.TYPE + "=?";
				args = new String[2];
				args[0] = uri.getPathSegments().get(1);
				args[1] = uri.getLastPathSegment();
				qb.setTables(Table.Comment.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
			case URI_CONTACTS:
				return TYPE_LIST_BASE + Table.LocalUser.NAME;
			case URI_COMMENTS:
				return TYPE_LIST_BASE + Table.Comment.NAME;
			case URI_PHONE_COMMENTS:
				return TYPE_LIST_BASE + Table.Comment.NAME;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final int uriId = uriMatcher.match(uri);
		int updated;
		switch (uriId) {
			case URI_COMMENTS:
				updated = update(uri, values, Table.Comment.ID + "=?",
								 new String[]{values.getAsString(Table.Comment.ID)});
				if (updated == 0) {
					getWritableDatabase().insert(Table.Comment.NAME, null, values);
				}
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String query, String[] args) {
		final int uriId = uriMatcher.match(uri);
		int deleted;
		switch (uriId) {
			case URI_PHONE_COMMENTS:
				query = Table.Comment.USER_ID + "=? AND " + Table.Comment.TYPE + "=?";
				args = new String[2];
				args[0] = uri.getPathSegments().get(1);
				args[1] = uri.getLastPathSegment();
				deleted = getWritableDatabase().delete(Table.Comment.NAME, query, args);
				break;
			case URI_CONTACTS:
				deleted = getWritableDatabase().delete(Table.LocalUser.NAME, query, args);
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		return deleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String query, String[] args) {
		final int uriId = uriMatcher.match(uri);
		int updated;
		switch (uriId) {
			case URI_CONTACTS:
				updated = getWritableDatabase().update(Table.LocalUser.NAME, values, query, args);
				break;
			case URI_COMMENTS:
				updated = getWritableDatabase().update(Table.Comment.NAME, values, query, args);
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		return updated;
	}

}