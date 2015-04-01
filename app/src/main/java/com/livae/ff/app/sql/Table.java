package com.livae.ff.app.sql;

import android.provider.BaseColumns;

public class Table {

	public static class User {

		public static final String NAME = "user";

		public static final String USER_NAME = NAME + "_" + "name";

		public static final String TAGLINE = NAME + "_" + "tagline";

		public static final String IMAGE_URL = NAME + "_" + "image_url";

		public static final String VOTES = NAME + "_" + "votes";

		public static final String APPS_SHARED = NAME + "_" + "apps_shared";

		public static final String COMMENTS = NAME + "_" + "comments";

		public static final String TIMES_FLAGGED = NAME + "_" + "times_flagged";

		public static final String ANONYMOUS = NAME + "_" + "anonymous";

		public static final String ID = BaseColumns._ID;

		public static final String T_ID = NAME + "." + ID;

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												ID + " INTEGER PRIMARY KEY NOT NULL, " +
												USER_NAME + " INTEGER, " +
												TAGLINE + " INTEGER, " +
												IMAGE_URL + " INTEGER, " +
												VOTES + " INTEGER, " +
												APPS_SHARED + " INTEGER, " +
												COMMENTS + " INTEGER, " +
												TIMES_FLAGGED + " INTEGER, " +
												ANONYMOUS + " INTEGER " +
												" );";
	}

	public static class Vote {

		public static final String NAME = "user_app_vote";

		public static final String ID = BaseColumns._ID;

		public static final String T_ID = NAME + "." + ID;

		public static final String DATE = NAME + "_" + "date";

		public static final String USER_ID = NAME + "_" + "user_id";

		public static final String APPLICATION_ENTRY_ID = NAME + "_" + "application_entry_id";

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												ID + " INTEGER PRIMARY KEY NOT NULL, " +
												DATE + " INTEGER, " +
												USER_ID + " INTEGER NOT NULL, " +
												APPLICATION_ENTRY_ID + " INTEGER NOT NULL " +
												" );";
	}

	public static class Comment {

		public static final String NAME = "comment";

		public static final String ID = BaseColumns._ID;

		public static final String T_ID = NAME + "." + ID;

		public static final String DATE = NAME + "_" + "date";

		public static final String USER_ID = NAME + "_" + "user_id";

		public static final String APPLICATION_ENTRY_ID = NAME + "_" + "application_entry_id";

		public static final String COMMENT = NAME + "_" + "comment";

		public static final String UP_VOTES = NAME + "_" + "up_votes";

		public static final String DOWN_VOTES = NAME + "_" + "down_votes";

		public static final String VOTE_TYPE = NAME + "_" + "vote_type";

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
												DATE + " INTEGER NOT NULL, " +
												USER_ID + " INTEGER NOT NULL, " +
												APPLICATION_ENTRY_ID + " INTEGER NOT NULL, " +
												COMMENT + " TEXT NOT NULL, " +
												UP_VOTES + " INTEGER, " +
												DOWN_VOTES + " INTEGER, " +
												VOTE_TYPE + " TEXT " +
												" );";
	}

}
