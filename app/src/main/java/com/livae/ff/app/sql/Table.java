package com.livae.ff.app.sql;

import android.provider.BaseColumns;

public class Table {

	public static class LocalUser {

		public static final String NAME = "local_user";

		public static final String CONTACT = NAME + "_" + "contact";

		public static final String IMAGE = NAME + "_" + "image";

		public static final String ACCEPTS_PRIVATE_ANONYMOUS =
		  NAME + "_" + "accepts_private_anonymous";

		public static final String BLOCKED = NAME + "_" + "blocked";

		public static final String PHONE = BaseColumns._ID;

		public static final String T_ID = NAME + "." + PHONE;

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												PHONE + " INTEGER NOT NULL, " +
												CONTACT + " TEXT, " +
												IMAGE + " TEXT, " +
												ACCEPTS_PRIVATE_ANONYMOUS + " INTEGER, " +
												BLOCKED + " INTEGER " +
												" );";
	}

	public static class Conversation {

		public static final String NAME = "conversation";

		public static final String ID = BaseColumns._ID;

		public static final String T_ID = NAME + "." + ID;

		public static final String TYPE = NAME + "_" + "type";

		public static final String PHONE = NAME + "_" + "phone";

		public static final String CONVERSATION_ALIAS = NAME + "_" + "conversation_alias";

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
												CONVERSATION_ALIAS + " TEXT, " +
												PHONE + " INTEGER, " +
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
												USER_ALIAS + " TEXT, " +
												PHONE + " INTEGER, " +
												COMMENT + " TEXT NOT NULL, " +
												DATE + " INTEGER NOT NULL, " +
												AGREE_VOTES + " INTEGER, " +
												DISAGREE_VOTES + " INTEGER, " +
												USER_VOTE_TYPE + " TEXT, " +
												VOTE_TYPE + " TEXT " +
												" );";
	}

}
