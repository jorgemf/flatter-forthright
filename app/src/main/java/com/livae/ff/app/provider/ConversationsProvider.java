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

public class ConversationsProvider extends AbstractProvider {

	private static final int URI_CONVERSATIONS = 1;

	private static final int URI_CONVERSATION = 2;

	private static final int URI_CONVERSATION_COMMENTS = 3;

	private static final int URI_COMMENTS = 4;

	private static final int URI_CONVERSATIONS_CONTACTS = 5;

	private static final int URI_COMMENTS_SYNC = 6;

	private static final int URI_COMMENT_SYNC = 7;

	private static Uri getContentUri() {
		return Uri.parse(CONTENT_URI_BASE + getAuthority(ConversationsProvider.class));
	}

	public static Uri getUriComments() {
		return Uri.withAppendedPath(getContentUri(), Table.Comment.NAME);
	}

	public static Uri getUriCommentsSync() {
		return Uri.withAppendedPath(getContentUri(), Table.CommentSync.NAME);
	}

	public static Uri getUriCommentSync(long commentId) {
		return ContentUris.withAppendedId(getUriCommentsSync(), commentId);
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

	public static Uri getUriConversationsContacts() {
		return Uri.withAppendedPath(getUriConversations(), Table.LocalUser.NAME);
	}

	@Override
	public boolean onCreate() {
		final boolean result = super.onCreate();
		final String authority = getAuthority(this.getClass());
		uriMatcher.addURI(authority, Table.Comment.NAME, URI_COMMENTS);
		uriMatcher.addURI(authority, Table.CommentSync.NAME, URI_COMMENTS_SYNC);
		uriMatcher.addURI(authority, Table.CommentSync.NAME + "/#/", URI_COMMENT_SYNC);
		uriMatcher.addURI(authority, Table.Conversation.NAME, URI_CONVERSATIONS);
		uriMatcher.addURI(authority, Table.Conversation.NAME + "/#/", URI_CONVERSATION);
		uriMatcher.addURI(authority, Table.Conversation.NAME + "/#/" +
									 Table.Comment.NAME, URI_CONVERSATION_COMMENTS);
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
			case URI_COMMENTS_SYNC:
				table = Table.CommentSync.NAME;
				for (ContentValues value : values) {
					db.insert(table, null, value);
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
			case URI_CONVERSATIONS:
				qb.setTables(Table.Conversation.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
			case URI_CONVERSATION_COMMENTS:
				String conversationId = "\'" + uri.getPathSegments().get(1) + "\'";
				String limit = order;
				String[] queries = new String[2];
				qb.setTables(Table.Comment.NAME);
				String[] qSelect;
				if (select != null) {
					qSelect = new String[select.length + 1];
					System.arraycopy(select, 0, qSelect, 0, select.length);
					qSelect[select.length] = Table.Comment.DATE + " AS date";
				} else {
					qSelect = new String[]{Table.Comment.DATE + " AS date"};
				}
				queries[0] = qb.buildQuery(qSelect,
										   Table.Comment.CONVERSATION_ID + "=" + conversationId,
										   null, null, "date", limit);
				String[] uSelect = {"1 AS " + Table.CommentSync.TEMP_SYNC,
									"1 AS " + Table.Comment.IS_ME,
									Table.CommentSync.DATE + " AS date",
									Table.CommentSync.DATE + " AS " + Table.Comment.DATE,
									Table.CommentSync.COMMENT + " AS " + Table.Comment.COMMENT,
									Table.CommentSync.USER_ALIAS + " AS " +
									Table.Comment.USER_ALIAS,
									Table.CommentSync.CONVERSATION_ID + " AS " +
									Table.Comment.CONVERSATION_ID};
				queries[1] = qb.buildQuery(uSelect, where, null, null, "date", limit);
				String query = qb.buildUnionQuery(queries, "date", limit);
				c = getReadableDatabase().rawQuery(query, args);
				break;
			case URI_CONVERSATIONS_CONTACTS:
				qb.setTables(Table.Conversation.NAME + " LEFT JOIN " + Table.LocalUser.NAME +
							 " ON " + Table.Conversation.PHONE + "=" + Table.LocalUser.PHONE);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
			case URI_COMMENTS_SYNC:
				qb.setTables(Table.CommentSync.NAME);
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
			case URI_COMMENTS:
				return TYPE_LIST_BASE + Table.Comment.NAME;
			case URI_COMMENTS_SYNC:
				return TYPE_LIST_BASE + Table.CommentSync.NAME;
			case URI_COMMENT_SYNC:
				return TYPE_ITEM_BASE + Table.CommentSync.NAME;
			case URI_CONVERSATION:
				return TYPE_ITEM_BASE + Table.Conversation.NAME;
			case URI_CONVERSATIONS:
				return TYPE_LIST_BASE + Table.Conversation.NAME;
			case URI_CONVERSATION_COMMENTS:
				return TYPE_LIST_BASE + Table.Comment.NAME;
			case URI_CONVERSATIONS_CONTACTS:
				return TYPE_LIST_BASE + Table.LocalUser.NAME;
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
			case URI_COMMENTS:
				updated = update(uri, null, null, null);
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
			case URI_COMMENT_SYNC:
				query = Table.CommentSync.ID + "=?";
				args = new String[1];
				args[0] = uri.getPathSegments().get(1);
				deleted = getWritableDatabase().delete(Table.CommentSync.NAME, query, args);
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
			case URI_COMMENTS:
				query = Table.Comment.ID + "=?";
				args = new String[1];
				args[0] = uri.getLastPathSegment();
				updated = getWritableDatabase().update(Table.Comment.NAME, values, query, args);
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		return updated;
	}

}