package com.livae.ff.app.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.livae.apphunt.app.sql.Table;
import com.livae.apphunt.common.Constants.Lang;

import java.util.List;

public class DataProvider extends AbstractProvider {

	private static final int URI_USERS = 1;

	private static final int URI_USER = 2;

	private static final int URI_APP_ENTRIES = 3;

	private static final int URI_APP_ENTRIES_LANG = 4;

	private static final int URI_APP_ENTRY = 5;

	private static final int URI_USERS_APPS_RELATIONSHIPS = 6;

	private static final int URI_USERS_APP_RELATIONSHIPS = 7;

	private static final int URI_COMMENTS = 8;

	private static final int URI_APP_COMMENTS = 9;

	private static final int URI_VOTES = 10;

	private static final int URI_APP_VOTES = 11;

	private static final int URI_USER_SHARED_APPS = 12;

	private static final int URI_USER_APPS_VOTES = 13;

	private static final int URI_USER_APPS_COMMENTS = 14;

	private static final int URI_USER_APPS_RELATIONSHIPS = 15;

	private static Uri getContentUri() {
		return Uri.parse(CONTENT_URI_BASE + getAuthority(DataProvider.class));
	}

	public static Uri getUriUsers() {
		return Uri.withAppendedPath(getContentUri(), Table.User.NAME);
	}

	public static Uri getUriUser(Long id) {
		return Uri.withAppendedPath(getUriUsers(), id.toString());
	}

	public static Uri getUriUserSharedApps(Long id) {
		return Uri.withAppendedPath(getUriUser(id), Table.AppEntry.NAME);
	}

	public static Uri getUriUserApplicationsVotes(long id) {
		return Uri.withAppendedPath(getUriUser(id), Table.Vote.NAME);
	}

	public static Uri getUriUserApplicationsComments(long id) {
		return Uri.withAppendedPath(getUriUser(id), Table.Comment.NAME);
	}

	public static Uri getUriUserApplicationsRelationships(long id) {
		return Uri.withAppendedPath(getUriUser(id), Table.UserApplicationRelated.NAME);
	}

	public static Uri getUriApplicationEntries() {
		return Uri.withAppendedPath(getContentUri(), Table.AppEntry.NAME);
	}

	public static Uri getUriApplicationEntries(Lang lang) {
		Uri uri;
		uri = Uri.withAppendedPath(getUriApplicationEntries(), "LANG");
		uri = Uri.withAppendedPath(uri, lang.name());
		return uri;
	}

	public static Uri getUriApplicationEntry(long id) {
		return Uri.withAppendedPath(getUriApplicationEntries(), Long.toString(id));
	}

//	public static Uri getUriApplications() {
//		return Uri.withAppendedPath(getContentUri(), Table.Application.NAME);
//	}
//
//	private static Uri getUriApplication(String id) {
//		return Uri.withAppendedPath(getUriApplications(), id);
//	}

	public static Uri getUriUsersApplicationsRelated() {
		return Uri.withAppendedPath(getContentUri(), Table.UserApplicationRelated.NAME);
	}

	public static Uri getUriUsersApplicationRelated(String id) {
		Uri appUri = Uri.withAppendedPath(getUriApplicationEntries(), id);
		return Uri.withAppendedPath(appUri, Table.UserApplicationRelated.NAME);
	}

	public static Uri getUriComments() {
		return Uri.withAppendedPath(getContentUri(), Table.Comment.NAME);
	}

	public static Uri getUriApplicationEntryComments(long id) {
		return Uri.withAppendedPath(getUriApplicationEntry(id), Table.Comment.NAME);
	}

	public static Uri getUriVotes() {
		return Uri.withAppendedPath(getContentUri(), Table.Vote.NAME);
	}

	public static Uri getUriApplicationEntryVotes(long id) {
		return Uri.withAppendedPath(getUriApplicationEntry(id), Table.Vote.NAME);
	}

	@Override
	public boolean onCreate() {
		final boolean result = super.onCreate();
		final String authority = getAuthority(this.getClass());
		uriMatcher.addURI(authority, Table.User.NAME, URI_USERS);
		uriMatcher.addURI(authority, Table.User.NAME + "/#", URI_USER);
		uriMatcher.addURI(authority, Table.User.NAME + "/#/" + Table.AppEntry.NAME,
						  URI_USER_SHARED_APPS);
		uriMatcher.addURI(authority, Table.User.NAME + "/#/" + Table.Vote.NAME,
						  URI_USER_APPS_VOTES);
		uriMatcher.addURI(authority, Table.User.NAME + "/#/" + Table.Comment.NAME,
						  URI_USER_APPS_COMMENTS);
		uriMatcher.addURI(authority, Table.User.NAME + "/#/" + Table.UserApplicationRelated.NAME,
						  URI_USER_APPS_RELATIONSHIPS);
		uriMatcher.addURI(authority, Table.AppEntry.NAME, URI_APP_ENTRIES);
		uriMatcher.addURI(authority, Table.AppEntry.NAME + "/#", URI_APP_ENTRY);
		uriMatcher.addURI(authority, Table.AppEntry.NAME + "/LANG/*", URI_APP_ENTRIES_LANG);
		uriMatcher.addURI(authority, Table.UserApplicationRelated.NAME,
						  URI_USERS_APPS_RELATIONSHIPS);
		uriMatcher.addURI(authority,
						  Table.AppEntry.NAME + "/*/" + Table.UserApplicationRelated.NAME,
						  URI_USERS_APP_RELATIONSHIPS);
		uriMatcher.addURI(authority, Table.Comment.NAME, URI_COMMENTS);
		uriMatcher.addURI(authority, Table.AppEntry.NAME + "/#/" + Table.Comment.NAME,
						  URI_APP_COMMENTS);
		uriMatcher.addURI(authority, Table.Vote.NAME, URI_VOTES);
		uriMatcher.addURI(authority, Table.AppEntry.NAME + "/#/" + Table.Vote.NAME, URI_APP_VOTES);
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
			case URI_USERS:
				table = Table.User.NAME;
				query = Table.User.ID + "=?";
				for (ContentValues value : values) {
					args[0] = value.getAsString(Table.User.ID);
					if (db.update(table, value, query, args) == 0) {
						db.insert(table, null, value);
					}
				}
				break;
			case URI_APP_ENTRIES:
				table = Table.AppEntry.NAME;
				query = Table.AppEntry.ID + "=?";
				for (ContentValues value : values) {
					args[0] = value.getAsString(Table.AppEntry.ID);
					if (db.update(table, value, query, args) == 0) {
						db.insert(table, null, value);
					}
				}
				break;
//			case URI_APPS:
//				table = Table.Application.NAME;
//				query = Table.Application.ID + "=?";
//				for (ContentValues value : values) {
//					args[0] = value.getAsString(Table.Application.ID);
//					if (db.update(table, value, query, args) == 0) {
//						db.insert(table, null, value);
//					}
//				}
//				break;
			case URI_USERS_APPS_RELATIONSHIPS:
				table = Table.UserApplicationRelated.NAME;
				query = Table.UserApplicationRelated.ID + "=?";
				for (ContentValues value : values) {
					args[0] = value.getAsString(Table.UserApplicationRelated.ID);
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
			case URI_VOTES:
				table = Table.Vote.NAME;
				query = Table.Vote.ID + "=?";
				for (ContentValues value : values) {
					args[0] = value.getAsString(Table.Vote.ID);
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
			case URI_USER:
				where = Table.User.ID + "=?";
				args = new String[]{uri.getLastPathSegment()};
				qb.setTables(Table.User.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, null);
				break;
			case URI_USERS:
				qb.setTables(Table.User.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, null);
				break;
			case URI_USER_SHARED_APPS:
				where = Table.AppEntry.USER_SHARED_ID + "=?";
				args = new String[]{pathSegments.get(pathSegments.size() - 2)};
				qb.setTables(Table.AppEntry.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, null);
				break;
			case URI_USER_APPS_COMMENTS:
				where = Table.Comment.USER_ID + "=?";
				args = new String[]{pathSegments.get(pathSegments.size() - 2)};
				qb.setTables(Table.Comment.NAME +
							 " LEFT JOIN " + Table.User.NAME + " ON " +
							 Table.Comment.USER_ID + "=" + Table.User.T_ID +
							 " LEFT JOIN " + Table.AppEntry.NAME + " ON " +
							 Table.Comment.APPLICATION_ENTRY_ID + "=" + Table.AppEntry.T_ID +
							 " LEFT JOIN " + Table.UserApplicationRelated.NAME + " ON " +
							 Table.UserApplicationRelated.APPLICATION_ID + "=" +
							 Table.AppEntry.APPLICATION_ID);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
			case URI_USER_APPS_RELATIONSHIPS:
				where = Table.UserApplicationRelated.USER_ID + "=?";
				args = new String[]{pathSegments.get(pathSegments.size() - 2)};
				qb.setTables(Table.UserApplicationRelated.NAME +
							 " LEFT JOIN " + Table.AppEntry.NAME + " ON " +
							 Table.AppEntry.T_ID + "=" +
							 Table.UserApplicationRelated.APPLICATION_ID);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
			case URI_USER_APPS_VOTES:
				where = Table.Vote.USER_ID + "=?";
				args = new String[]{pathSegments.get(pathSegments.size() - 2)};
				qb.setTables(Table.Vote.NAME +
							 " LEFT JOIN " + Table.AppEntry.NAME + " ON " +
							 Table.AppEntry.T_ID + "=" + Table.Vote.APPLICATION_ENTRY_ID +
							 " LEFT JOIN " + Table.UserApplicationRelated.NAME + " ON " +
							 Table.UserApplicationRelated.APPLICATION_ID + "=" +
							 Table.AppEntry.APPLICATION_ID);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
			case URI_APP_ENTRIES:
				qb.setTables(Table.AppEntry.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
			case URI_APP_ENTRIES_LANG:
				if (where != null) {
					where = "( " + where + " ) AND " + Table.AppEntry.LANG + "=?";
					String[] newArgs = new String[args.length + 1];
					System.arraycopy(args, 0, newArgs, 0, args.length);
					newArgs[args.length] = uri.getLastPathSegment();
					args = newArgs;
				} else {
					where = Table.AppEntry.LANG + "=?";
					args = new String[]{uri.getLastPathSegment()};
				}
				qb.setTables(Table.AppEntry.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
			case URI_APP_ENTRY:
				where = Table.AppEntry.T_ID + "=?";
				args = new String[]{uri.getLastPathSegment()};
				qb.setTables(Table.AppEntry.NAME +
							 " LEFT JOIN " + Table.User.NAME + " ON " +
							 Table.AppEntry.USER_SHARED_ID + "=" + Table.User.T_ID +
							 " LEFT JOIN " + Table.UserApplicationRelated.NAME + " ON " +
							 Table.AppEntry.T_ID + "=" +
							 Table.UserApplicationRelated.APPLICATION_ID + " AND " +
							 Table.User.T_ID + "=" +
							 Table.UserApplicationRelated.USER_ID);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, null);
				break;
			case URI_USERS_APP_RELATIONSHIPS:
				where = Table.UserApplicationRelated.APPLICATION_ID + "=?";
				args = new String[]{pathSegments.get(pathSegments.size() - 2)};
				qb.setTables(Table.UserApplicationRelated.NAME);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, null);
				break;
			case URI_APP_COMMENTS:
				where = Table.Comment.APPLICATION_ENTRY_ID + "=?";
				args = new String[]{pathSegments.get(pathSegments.size() - 2)};
				// no break!
			case URI_COMMENTS:
				qb.setTables(Table.Comment.NAME +
							 " LEFT JOIN " + Table.User.NAME + " ON " +
							 Table.Comment.USER_ID + "=" + Table.User.T_ID +
							 " LEFT JOIN " + Table.AppEntry.NAME + " ON " +
							 Table.Comment.APPLICATION_ENTRY_ID + "=" + Table.AppEntry.T_ID +
							 " LEFT JOIN " + Table.UserApplicationRelated.NAME + " ON " +
							 Table.UserApplicationRelated.APPLICATION_ID + "=" +
							 Table.AppEntry.APPLICATION_ID + " AND " +
							 Table.UserApplicationRelated.USER_ID + "=" +
							 Table.Comment.USER_ID);
				c = qb.query(getReadableDatabase(), select, where, args, null, null, order);
				break;
			case URI_APP_VOTES:
				where = Table.Vote.APPLICATION_ENTRY_ID + "=?";
				args = new String[]{pathSegments.get(pathSegments.size() - 2)};
				order = "-" + Table.Vote.DATE;
				qb.setTables(Table.Vote.NAME +
							 " LEFT JOIN " + Table.User.NAME + " ON " +
							 Table.Vote.USER_ID + "=" + Table.User.T_ID +
							 " LEFT JOIN " + Table.AppEntry.NAME + " ON " +
							 Table.Vote.APPLICATION_ENTRY_ID + "=" + Table.AppEntry.T_ID +
							 " LEFT JOIN " + Table.UserApplicationRelated.NAME + " ON " +
							 Table.UserApplicationRelated.APPLICATION_ID + "=" +
							 Table.AppEntry.APPLICATION_ID);
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
			case URI_USERS:
				return TYPE_LIST_BASE + Table.User.NAME;
			case URI_USER:
				return TYPE_ITEM_BASE + Table.User.NAME;
			case URI_APP_ENTRIES:
				return TYPE_LIST_BASE + Table.AppEntry.NAME;
			case URI_APP_ENTRY:
				return TYPE_ITEM_BASE + Table.AppEntry.NAME;
//			case URI_APPS:
//				return TYPE_LIST_BASE + Table.Application.NAME;
			case URI_USERS_APPS_RELATIONSHIPS:
				return TYPE_LIST_BASE + Table.UserApplicationRelated.NAME;
			case URI_USERS_APP_RELATIONSHIPS:
				return TYPE_LIST_BASE + Table.UserApplicationRelated.NAME;
			case URI_COMMENTS:
				return TYPE_LIST_BASE + Table.Comment.NAME;
			case URI_APP_COMMENTS:
				return TYPE_LIST_BASE + Table.Comment.NAME;
			case URI_VOTES:
				return TYPE_LIST_BASE + Table.Vote.NAME;
			case URI_APP_VOTES:
				return TYPE_LIST_BASE + Table.Vote.NAME;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final int uriId = uriMatcher.match(uri);
		int updated;
		switch (uriId) {
			case URI_USERS:
				updated = update(uri, values, Table.User.ID + "=?",
								 new String[]{values.getAsString(Table.User.ID)});
				if (updated == 0) {
					getWritableDatabase().insert(Table.User.NAME, null, values);
				}
				break;
			case URI_APP_ENTRIES:
				updated = update(uri, values, Table.AppEntry.ID + "=?",
								 new String[]{values.getAsString(Table.AppEntry.ID)});
				if (updated == 0) {
					getWritableDatabase().insert(Table.AppEntry.NAME, null, values);
				}
//			case URI_APPS:
//				updated = update(uri, values, Table.Application.ID + "=?",
//								 new String[]{values.getAsString(Table.Application.ID)});
//				if (updated == 0) {
//					getWritableDatabase().insert(Table.Application.NAME, null, values);
//				}
//				break;
			case URI_USERS_APPS_RELATIONSHIPS:
				updated = update(uri, values, Table.UserApplicationRelated.ID + "=?",
								 new String[]{values.getAsString(Table.UserApplicationRelated.ID)});
				if (updated == 0) {
					getWritableDatabase().insert(Table.UserApplicationRelated.NAME, null, values);
				}
				break;
			case URI_COMMENTS:
				updated = update(uri, values, Table.Comment.ID + "=?",
								 new String[]{values.getAsString(Table.Comment.ID)});
				if (updated == 0) {
					getWritableDatabase().insert(Table.Comment.NAME, null, values);
				}
				break;
			case URI_VOTES:
				updated = update(uri, values, Table.Vote.ID + "=?",
								 new String[]{values.getAsString(Table.Vote.ID)});
				if (updated == 0) {
					getWritableDatabase().insert(Table.Vote.NAME, null, values);
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
			case URI_USERS:
				deleted = getWritableDatabase().delete(Table.User.NAME, query, args);
				break;
			case URI_APP_ENTRIES:
				deleted = getWritableDatabase().delete(Table.AppEntry.NAME, query, args);
				break;
//			case URI_APPS:
//				deleted = getWritableDatabase().delete(Table.Application.NAME, query, args);
//				break;
			case URI_USERS_APPS_RELATIONSHIPS:
				deleted = getWritableDatabase().delete(Table.UserApplicationRelated.NAME, query,
													   args);
				break;
			case URI_COMMENTS:
				deleted = getWritableDatabase().delete(Table.Comment.NAME, query, args);
				break;
			case URI_VOTES:
				deleted = getWritableDatabase().delete(Table.Vote.NAME, query, args);
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
			case URI_USERS:
				updated = getWritableDatabase().update(Table.User.NAME, values, query, args);
				break;
			case URI_APP_ENTRIES:
				updated = getWritableDatabase().update(Table.AppEntry.NAME, values, query, args);
				break;
//			case URI_APPS:
//				updated = getWritableDatabase().update(Table.Application.NAME, values, query, args);
//				break;
			case URI_USERS_APPS_RELATIONSHIPS:
				updated = getWritableDatabase().update(Table.UserApplicationRelated.NAME, values,
													   query, args);
				break;
			case URI_COMMENTS:
				updated = getWritableDatabase().update(Table.Comment.NAME, values, query, args);
				break;
			case URI_APP_VOTES:
				updated = getWritableDatabase().update(Table.Vote.NAME, values, query, args);
				break;
			default:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		return updated;
	}

}