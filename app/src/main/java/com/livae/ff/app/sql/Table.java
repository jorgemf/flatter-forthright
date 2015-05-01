package com.livae.ff.app.sql;

import android.provider.BaseColumns;

public class Table {

	public static class LocalUser {

		public static final String NAME = "local_user";

		public static final String PHONE = NAME + "_" + "phone";

		public static final String CONTACT = NAME + "_" + "contact";

		public static final String IMAGE = NAME + "_" + "image";

		public static final String FAVORITE = NAME + "_" + "favorite";

		public static final String ID = BaseColumns._ID;

		public static final String T_ID = NAME + "." + ID;

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
												PHONE + " INTEGER NOT NULL, " +
												CONTACT + " TEXT, " +
												IMAGE + " TEXT, " +
												FAVORITE + " INTEGER " +
												" );";
	}

	public static class Comment {

		public static final String NAME = "comment";

		public static final String DATE = NAME + "_" + "date";

		public static final String USER_ID = NAME + "_" + "user_id";

		public static final String IS_ME = NAME + "_" + "is_me";

		public static final String PHONE = NAME + "_" + "phone";

		public static final String TYPE = NAME + "_" + "type";

		public static final String COMMENT = NAME + "_" + "comment";

		public static final String AGREE_VOTES = NAME + "_" + "agree_votes";

		public static final String DISAGREE_VOTES = NAME + "_" + "disagree_votes";

		public static final String USER_VOTE_TYPE = NAME + "_" + "user_vote_type";

		public static final String VOTE_TYPE = NAME + "_" + "vote_type";

		public static final String ID = BaseColumns._ID;

		public static final String T_ID = NAME + "." + ID;

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
												DATE + " INTEGER NOT NULL, " +
												PHONE + " INTEGER NOT NULL, " +
												USER_ID + " INTEGER NOT NULL, " +
												IS_ME + " INTEGER NOT NULL, " +
												COMMENT + " TEXT NOT NULL, " +
												TYPE + " TEXT NOT NULL, " +
												AGREE_VOTES + " INTEGER, " +
												DISAGREE_VOTES + " INTEGER, " +
												USER_VOTE_TYPE + " TEXT, " +
												VOTE_TYPE + " TEXT " +
												" );";
	}

}
