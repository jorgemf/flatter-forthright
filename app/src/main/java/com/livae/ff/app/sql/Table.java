package com.livae.ff.app.sql;

import android.provider.BaseColumns;

public class Table {

	public static class LocalUser {

		public static final String NAME = "local_user";

		public static final String PHONE = NAME + "_" + "phone";

		public static final String CONTACT = NAME + "_" + "contact";

		public static final String IMAGE = NAME + "_" + "image";

		public static final String ACCEPTS_PRIVATE_ANONYMOUS =
		  NAME + "_" + "accepts_private_anonymous";

		public static final String ID = BaseColumns._ID;

		public static final String T_ID = NAME + "." + ID;

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
												PHONE + " INTEGER NOT NULL, " +
												CONTACT + " TEXT, " +
												IMAGE + " TEXT, " +
												ACCEPTS_PRIVATE_ANONYMOUS + " INTEGER " +
												" );";
	}

	public static class Conversation {

		public static final String NAME = "conversation";

		public static final String ID = BaseColumns._ID;

		public static final String T_ID = NAME + "." + ID;

		public static final String TYPE = NAME + "_" + "type";

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
												TYPE + " TEXT NOT NULL, " +
												" );";
	}

	public static class Comment {

		public static final String NAME = "comment";

		public static final String DATE = NAME + "_" + "date";

		public static final String CONVERSATION_ID = NAME + "_" + "conversation_id";

		public static final String USER_ANONYMOUS_ID = NAME + "_" + "user_anonymous_id";

		public static final String USER_ALIAS = NAME + "_" + "user_alias";

		public static final String PHONE = NAME + "_" + "phone";

		public static final String COMMENT = NAME + "_" + "comment";

		public static final String AGREE_VOTES = NAME + "_" + "agree_votes";

		public static final String DISAGREE_VOTES = NAME + "_" + "disagree_votes";

		public static final String USER_VOTE_TYPE = NAME + "_" + "user_vote_type";

		public static final String VOTE_TYPE = NAME + "_" + "vote_type";

		public static final String ID = BaseColumns._ID;

		public static final String T_ID = NAME + "." + ID;

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
												CONVERSATION_ID + " INTEGER NOT NULL, " +
												USER_ANONYMOUS_ID + " INTEGER, " +
												PHONE + " INTEGER, " +
												COMMENT + " TEXT NOT NULL, " +
												USER_ALIAS + " TEXT, " +
												DATE + " INTEGER NOT NULL, " +
												AGREE_VOTES + " INTEGER, " +
												DISAGREE_VOTES + " INTEGER, " +
												USER_VOTE_TYPE + " TEXT, " +
												VOTE_TYPE + " TEXT " +
												" );";
	}

}
