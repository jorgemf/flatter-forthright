package com.livae.ff.app.provider;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.livae.ff.app.sql.Table;

import java.util.List;

public class DataProvider extends AbstractProvider {

	private static final int URI_CONTACTS = 1;

	private static final int URI_CONTACT = 2;

	private static final int URI_CONVERSATIONS = 3;

	private static final int URI_CONVERSATION = 4;

	private static final int URI_CONVERSATION_COMMENTS = 5;

	private static final int URI_COMMENTS = 6;

	private static final int URI_CONTACTS_CONVERSATIONS = 7;

	private static final int URI_CONVERSATIONS_CONTACTS = 8;

	private static Uri getContentUri() {
		return Uri.parse(CONTENT_URI_BASE + getAuthority(DataProvider.class));
	}

	public static Uri getUriComments() {
		return Uri.withAppendedPath(getContentUri(), Table.Comment.NAME);
	}

	public static Uri getUriContacts() {
		return Uri.withAppendedPath(getContentUri(), Table.LocalUser.NAME);
	}

	public static Uri getUriContact(Long id) {
		return ContentUris.withAppendedId(getUriContacts(), id);
	}

	public static Uri getUriConversations() {
		return Uri.withAppendedPath(getContentUri(), Table.Conversation.NAME);
	}

	public static Uri getUriConversation(Long conversationId) {
		return ContentUris.withAppendedId(getUriConversations(), conversationId);
	}

	public static Uri getUriConversationComments(Long conversationId) {
		return Uri.withAppendedPath(getUriConversation(conversationId), Table.Comment.NAME);
	}

	public static Uri getUriContactsConversations() {
		return Uri.withAppendedPath(getUriContacts(), Table.Conversation.NAME);
	}

	public static Uri getUriConversationsContacts() {
		return Uri.withAppendedPath(getUriConversations(), Table.LocalUser.NAME);
	}

	@Override
	public boolean onCreate() {
		final boolean result = super.onCreate();
		final String authority = getAuthority(this.getClass());
		uriMatcher.addURI(authority, Table.LocalUser.NAME, URI_CONTACTS);
		uriMatcher.addURI(authority, Table.LocalUser.NAME + "/#/", URI_CONTACT);
		uriMatcher.addURI(authority, Table.Comment.NAME, URI_COMMENTS);
		uriMatcher.addURI(authority, Table.Conversation.NAME, URI_CONVERSATIONS);
		uriMatcher.addURI(authority, Table.Conversation.NAME + "/#/", URI_CONVERSATION);
		uriMatcher.addURI(authority, Table.Conversation.NAME + "/#/" +
									 Table.Comment.NAME, URI_CONVERSATION_COMMENTS);
		uriMatcher.addURI(authority, Table.LocalUser.NAME + "/" + Table.Conversation.NAME,
						  URI_CONTACTS_CONVERSATIONS);
		uriMatcher.addURI(authority, Table.Conversation.NAME + "/" + Table.LocalUser.NAME,
						  URI_CONVERSATIONS_CONTACTS);
		return result;
	}

	@Override
	public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
		final int uriId = uriMatcher.match(uri);
		final int numValues = values.length;
//		String query;
		String table;
//		String[] args = new String[1];
		final SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		switch (uriId) {
			case URI_CONTACTS:
				table = Table.LocalUser.NAME;
				for (ContentValues value : values) {
					db.insert(table, null, value);
				}
				break;
			case URI_CONVERSATIONS:
				table = Table.Conversation.NAME;
				for (ContentValues value : values) {
					db.insert(table, null, value);
				}
				break;
			case URI_COMMENTS:
				table = Table.Comment.NAME;
				for (ContentValues value : values) {
					db.insert(table, null, value);
				}
				break;
//			case URI_CONTACTS:
//				table = Table.LocalUser.NAME;
//				query = Table.LocalUser.PHONE + "=?";
//				for (ContentValues value : values) {
//					args[0] = value.getAsString(Table.LocalUser.PHONE);
//					if (db.update(table, value, query, args) == 0) {
//						db.insert(table, null, value);
//					}
//				}
//				break;
//			case URI_CONVERSATIONS:
//				table = Table.Conversation.NAME;
//				query = Table.Conversation.ID + "=?";
//				for (ContentValues value : values) {
//					args[0] = value.getAsString(Table.Conversation.ID);
//					if (db.update(table, value, query, args) == 0) {
//						db.insert(table, null, value);
//					}
//				}
//				break;
//			case URI_COMMENTS:
//				table = Table.Comment.NAME;
//				query = Table.Comment.ID + "=?";
//				for (ContentValues value : values) {
//					args[0] = value.getAsString(Table.Comment.ID);
//					if (db.update(table, value, query, args) == 0) {
//						db.insert(table, null, value);
//					}
//				}
//				break;
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
			case URI_CONTACT:
				where = Table.LocalUser.T_ID + "=?";
				args = new String[1];
				args[0] = uri.getLastPathSegment();
				qb.setTables(Table.LocalUser.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
			case URI_CONVERSATIONS:
				qb.setTables(Table.Conversation.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
			case URI_CONVERSATION_COMMENTS:
				where = Table.Comment.CONVERSATION_ID + "=?";
				args = new String[1];
				args[0] = uri.getPathSegments().get(1);
				qb.setTables(Table.Comment.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
			case URI_CONTACTS_CONVERSATIONS:
				qb.setTables(Table.LocalUser.NAME + " LEFT JOIN " + Table.Conversation.NAME +
							 " ON " + Table.LocalUser.PHONE + "=" + Table.Conversation.PHONE);
				String selection = Table.LocalUser.IS_MOBILE_NUMBER + " AND ( " +
								   Table.Conversation.TYPE + " IS NULL OR " +
								   Table.Conversation.TYPE +
								   "=? )";
//				String[] selectionArgs = new String[]{Constants.ChatType.FLATTER.name()};
//				c = qb.query(getReadableDatabase(), select, selection, selectionArgs, null, null,
//							 order + " LIMIT 10");
//				Debug.print(c);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
//				Debug.print(c);
				break;
			case URI_CONVERSATIONS_CONTACTS:
				qb.setTables(Table.Conversation.NAME + " LEFT JOIN " + Table.LocalUser.NAME +
							 " ON " + Table.Conversation.PHONE + "=" + Table.LocalUser.PHONE);
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
			case URI_CONTACT:
				return TYPE_ITEM_BASE + Table.LocalUser.NAME;
			case URI_COMMENTS:
				return TYPE_LIST_BASE + Table.Comment.NAME;
			case URI_CONVERSATION:
				return TYPE_ITEM_BASE + Table.Conversation.NAME;
			case URI_CONVERSATIONS:
				return TYPE_LIST_BASE + Table.Conversation.NAME;
			case URI_CONVERSATION_COMMENTS:
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
			case URI_CONVERSATION:
				updated = update(uri, null, null, null);
				if (updated == 0) {
					getWritableDatabase().insert(Table.Conversation.NAME, null, values);
				}
				break;
			case URI_CONTACT:
				updated = update(uri, null, null, null);
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
			case URI_CONVERSATION:
				query = Table.Conversation.ID + "=?";
				args = new String[1];
				args[0] = uri.getLastPathSegment();
				deleted = getWritableDatabase().delete(Table.Conversation.NAME, query, args);
				break;
			case URI_CONVERSATION_COMMENTS:
				query = Table.Comment.CONVERSATION_ID + "=?";
				args = new String[1];
				args[0] = uri.getPathSegments().get(1);
				deleted = getWritableDatabase().delete(Table.Comment.NAME, query, args);
				break;
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
			case URI_CONVERSATION:
				query = Table.Conversation.ID + "=?";
				args = new String[1];
				args[0] = uri.getLastPathSegment();
				updated = getWritableDatabase().update(Table.Conversation.NAME, values, query,
													   args);
				break;
			case URI_CONTACT:
				query = Table.LocalUser.T_ID + "=?";
				args = new String[1];
				args[0] = uri.getLastPathSegment();
				updated = getWritableDatabase().update(Table.LocalUser.NAME, values, query, args);
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		return updated;
	}

}