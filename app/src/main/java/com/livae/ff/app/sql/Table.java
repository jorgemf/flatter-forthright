package com.livae.ff.app.sql;

import android.provider.BaseColumns;

public class Table {

	public static class LocalUser {

		public static final String NAME = "local_user";

		public static final String CONTACT_NAME = NAME + "_" + "contact_name";

		public static final String IMAGE_URI = NAME + "_" + "image_uri";

		public static final String ACCEPTS_PRIVATE = NAME + "_" + "accepts_private";

		public static final String IS_MOBILE_NUMBER = NAME + "_" + "is_mobile_number";

		public static final String BLOCKED = NAME + "_" + "blocked";

		public static final String PHONE = NAME + "_" + "phone";

		public static final String ANDROID_RAW_CONTACT_ID = NAME + "_" + "android_raw_contact_id";

		public static final String ANDROID_RAW_CONTACT_LAST_VERSION =
		  NAME + "_" + "android_raw_contact_last_version";

		public static final String ID = BaseColumns._ID;

		public static final String T_ID = NAME + "." + ID;

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
												PHONE + " INTEGER, " +
												CONTACT_NAME + " TEXT, " +
												IMAGE_URI + " TEXT, " +
												ACCEPTS_PRIVATE + " INTEGER, " +
												IS_MOBILE_NUMBER + " INTEGER, " +
												BLOCKED + " INTEGER, " +
												ANDROID_RAW_CONTACT_ID + " INTEGER, " +
												ANDROID_RAW_CONTACT_LAST_VERSION + " INTEGER " +
												" );";
	}

	public static class Conversation {

		public static final String NAME = "conversation";

		public static final String ID = BaseColumns._ID;

		public static final String T_ID = NAME + "." + ID;

		public static final String TYPE = NAME + "_" + "type";

		public static final String PHONE = NAME + "_" + "phone";

		public static final String ROOM_NAME = NAME + "_" + "room_name";

		public static final String LAST_ACCESS = NAME + "_" + "last_access";

		public static final String LAST_MESSAGE_DATE = NAME + "_" + "last_message_date";

		public static final String LAST_MESSAGE = NAME + "_" + "last_message";

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
												ROOM_NAME + " TEXT, " +
												PHONE + " INTEGER, " +
												LAST_ACCESS + " INTEGER, " +
												LAST_MESSAGE + " TEXT, " +
												LAST_MESSAGE_DATE + " INTEGER, " +
												TYPE + " TEXT NOT NULL " +
												" );";
	}

	public static class Comment {

		public static final String NAME = "comment";

		public static final String DATE = NAME + "_" + "date";

		public static final String CONVERSATION_ID = NAME + "_" + "conversation_id";

		public static final String USER_ANONYMOUS_ID = NAME + "_" + "user_anonymous_id";

		public static final String USER_ALIAS = NAME + "_" + "user_alias";

		public static final String IS_ME = NAME + "_" + "is_me";

		public static final String COMMENT = NAME + "_" + "comment";

		public static final String AGREE_VOTES = NAME + "_" + "agree_votes";

		public static final String DISAGREE_VOTES = NAME + "_" + "disagree_votes";

		public static final String USER_VOTE_TYPE = NAME + "_" + "user_vote_type";

		public static final String VOTE_TYPE = NAME + "_" + "vote_type";

		public static final String USER_MARK = NAME + "_" + "user_mark";

		public static final String TIMES_FLAGGED = NAME + "_" + "times_flagged";

		public static final String TIMES_FLAGGED_ABUSE = NAME + "_" + "times_flagged_abuse";

		public static final String TIMES_FLAGGED_INSULT = NAME + "_" + "times_flagged_insult";

		public static final String TIMES_FLAGGED_LIE = NAME + "_" + "times_flagged_lie";

		public static final String TIMES_FLAGGED_OTHER = NAME + "_" + "times_flagged_other";

		public static final String ID = BaseColumns._ID;

		public static final String T_ID = NAME + "." + ID;

		public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + NAME + " ( " +
												ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
												CONVERSATION_ID + " INTEGER NOT NULL, " +
												USER_ANONYMOUS_ID + " INTEGER, " +
												USER_ALIAS + " TEXT, " +
												IS_ME + " INTEGER, " +
												COMMENT + " TEXT NOT NULL, " +
												DATE + " INTEGER NOT NULL, " +
												AGREE_VOTES + " INTEGER, " +
												DISAGREE_VOTES + " INTEGER, " +
												USER_VOTE_TYPE + " TEXT, " +
												VOTE_TYPE + " TEXT, " +
												USER_MARK + " TEXT, " +
												TIMES_FLAGGED + " INTEGER, " +
												TIMES_FLAGGED_ABUSE + " INTEGER, " +
												TIMES_FLAGGED_INSULT + " INTEGER, " +
												TIMES_FLAGGED_LIE + " INTEGER, " +
												TIMES_FLAGGED_OTHER + " INTEGER " +
												" );";
	}

}
