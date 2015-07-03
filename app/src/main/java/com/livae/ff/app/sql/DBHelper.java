package com.livae.ff.app.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String NAME = "ff.sqlite";

	private static final int VERSION = 1;

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
			case 1: // current version
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
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.LocalUser.NAME);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.Conversation.NAME);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.Comment.NAME);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.CommentSync.NAME);
		onCreate(sqLiteDatabase);
	}
}