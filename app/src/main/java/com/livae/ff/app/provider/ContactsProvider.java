package com.livae.ff.app.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.livae.ff.app.sql.Table;
import com.livae.ff.common.Constants;

import java.util.List;

import javax.annotation.Nonnull;

public class ContactsProvider extends AbstractProvider {

	private static final int URI_CONTACTS = 1;

	private static final int URI_CONTACT = 2;

	private static final int URI_CONTACTS_CONVERSATIONS = 3;

	private static Uri getContentUri() {
		return Uri.parse(CONTENT_URI_BASE + getAuthority(ContactsProvider.class));
	}

	public static Uri getUriContacts() {
		return Uri.withAppendedPath(getContentUri(), Table.LocalUser.NAME);
	}

	public static Uri getUriContact(Long id) {
		return ContentUris.withAppendedId(getUriContacts(), id);
	}

	public static Uri getUriContactsConversations(Constants.ChatType chatType) {
		return Uri.withAppendedPath(Uri.withAppendedPath(getUriContacts(), Table.Conversation.NAME),
									chatType.name());
	}

	@Override
	public boolean onCreate() {
		final boolean result = super.onCreate();
		final String authority = getAuthority(this.getClass());
		uriMatcher.addURI(authority, Table.LocalUser.NAME, URI_CONTACTS);
		uriMatcher.addURI(authority, Table.LocalUser.NAME + "/#/", URI_CONTACT);
		uriMatcher.addURI(authority, Table.LocalUser.NAME + "/" + Table.Conversation.NAME + "/*/",
						  URI_CONTACTS_CONVERSATIONS);
		return result;
	}

	@Override
	public int bulkInsert(Uri uri, @Nonnull ContentValues[] values) {
		final int uriId = uriMatcher.match(uri);
		final int numValues = values.length;
		String table;
		final SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		switch (uriId) {
			case URI_CONTACTS:
				table = Table.LocalUser.NAME;
				String query = Table.LocalUser.PHONE + "=?";
				String[] args = new String[1];
				for (ContentValues value : values) {
					args[0] = value.getAsString(Table.LocalUser.PHONE);
					if (db.update(Table.LocalUser.NAME, value, query, args) == 0) {
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
				qb.setDistinct(true);
				c = qb.query(getReadableDatabase(), select, where, args, Table.LocalUser.PHONE,
							 null, order);
				// loader manager will reload the cursor automatically
				c.setNotificationUri(getContext().getContentResolver(), uri);
				break;
			case URI_CONTACT:
				where = Table.LocalUser.T_ID + "=?";
				args = new String[1];
				args[0] = uri.getLastPathSegment();
				qb.setTables(Table.LocalUser.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				c.setNotificationUri(getContext().getContentResolver(), uri);
				break;
			case URI_CONTACTS_CONVERSATIONS:
				String chatType = uri.getLastPathSegment();
				qb.setTables(Table.LocalUser.NAME + " LEFT JOIN " + Table.Conversation.NAME +
							 " ON " + Table.LocalUser.PHONE + "=" + Table.Conversation.PHONE +
							 " AND " + Table.Conversation.TYPE + "=\'" + chatType + "\'");
				qb.setDistinct(true);
				c = qb.query(getReadableDatabase(), select, where, args, Table.LocalUser.PHONE,
							 null, order);
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
			case URI_CONTACT:
				return TYPE_ITEM_BASE + Table.LocalUser.NAME;
			case URI_CONTACTS_CONVERSATIONS:
				return TYPE_LIST_BASE + Table.Conversation.NAME;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final int uriId = uriMatcher.match(uri);
		int updated;
		switch (uriId) {
			case URI_CONTACT:
				updated = update(uri, values, null, null);
				if (updated == 0) {
					getWritableDatabase().insert(Table.LocalUser.NAME, null, values);
				}
				break;
			case URI_CONTACTS:
				long id = getWritableDatabase().insert(Table.LocalUser.NAME, null, values);
				return getUriContact(id);
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
			case URI_CONTACT:
				query = Table.LocalUser.T_ID + "=?";
				args = new String[1];
				args[0] = uri.getLastPathSegment();
				// no break
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
			case URI_CONTACT:
				query = Table.LocalUser.T_ID + "=?";
				args = new String[1];
				args[0] = uri.getLastPathSegment();
				updated = getWritableDatabase().update(Table.LocalUser.NAME, values, query, args);
				break;
			case URI_CONTACTS:
				updated = getWritableDatabase().update(Table.LocalUser.NAME, values, query, args);
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		return updated;
	}

}
