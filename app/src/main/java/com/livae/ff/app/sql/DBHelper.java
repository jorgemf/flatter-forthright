package com.livae.ff.app.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.livae.ff.app.Application;
import com.livae.ff.app.settings.Chats;
import com.livae.ff.common.Constants;

public class DBHelper extends SQLiteOpenHelper {

	private static final String NAME = "ff.sqlite";

	private static final int VERSION = 5;

	private static DBHelper instance;

	private DBHelper(Context context) {
		super(context, NAME, null, VERSION);
	}

	public static DBHelper instance(Context context) {
		if (instance == null) {
			Context appContext = context.getApplicationContext();
			instance = new DBHelper(appContext);
		}
		return instance;
	}

	public static long getSize(Context context) {
		return context.getDatabasePath(NAME).length();
	}

	public static void clearData(Context context) {
		instance(context).clearData();
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		try {
			sqLiteDatabase.execSQL(Table.LocalUser.CREATE_SQL);
			sqLiteDatabase.execSQL(Table.Conversation.CREATE_SQL);
			sqLiteDatabase.execSQL(Table.Comment.CREATE_SQL);
			sqLiteDatabase.execSQL(Table.CommentSync.CREATE_SQL);
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
		switch (oldVersion) {
			case 1:
				// sorry users for losing your old comments
				sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.Comment._NAME);
				sqLiteDatabase.execSQL(Table.Comment.CREATE_SQL);
			case 2:
				// bug in public conversations
				sqLiteDatabase.execSQL("DELETE FROM " + Table.Conversation._NAME +
									   " WHERE " + Table.Conversation.TYPE + " IN (\'" +
									   Constants.ChatType.FORTHRIGHT.name() + "', '" +
									   Constants.ChatType.FLATTER.name() + "')");
				final Chats chats = Application.appUser().getChats();
				chats.setChatFlatterId(null);
				chats.setChatForthrightId(null);
			case 3:
				// added notification colors and sounds
				sqLiteDatabase.execSQL("ALTER TABLE " + Table.Conversation._NAME + " ADD " +
									   Table.Conversation.NOTIFICATION_COLOR + " INTEGER ");
				sqLiteDatabase.execSQL("ALTER TABLE " + Table.Conversation._NAME + " ADD " +
									   Table.Conversation.NOTIFICATION_SOUND + " TEXT ");
				sqLiteDatabase.execSQL("ALTER TABLE " + Table.Conversation._NAME + " ADD " +
									   Table.Conversation.NOTIFICATION_MUTED + " INTEGER ");
			case 4:
				sqLiteDatabase.execSQL("ALTER TABLE " + Table.Comment._NAME + " ADD " +
									   Table.Comment.DATE_CREATED + " INTEGER");
			case 5: // current version
		}
	}

	@Override
	public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
		// no downgrades
		clearData(sqLiteDatabase);
	}

	private void clearData() {
		SQLiteDatabase sqLiteDatabase = getWritableDatabase();
		clearData(sqLiteDatabase);
	}

	private void clearData(SQLiteDatabase sqLiteDatabase) {
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.LocalUser._NAME);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.Conversation._NAME);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.Comment._NAME);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.CommentSync._NAME);
		onCreate(sqLiteDatabase);
	}
}
