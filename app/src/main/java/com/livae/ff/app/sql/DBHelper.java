package com.livae.ff.app.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String NAME = "apphunt.sqlite";

	private static final int VERSION = 2;

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
			sqLiteDatabase.execSQL(Table.AppEntry.CREATE_SQL);
			sqLiteDatabase.execSQL(Table.Comment.CREATE_SQL);
			sqLiteDatabase.execSQL(Table.User.CREATE_SQL);
			sqLiteDatabase.execSQL(Table.UserApplicationRelated.CREATE_SQL);
			sqLiteDatabase.execSQL(Table.Vote.CREATE_SQL);
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
		switch (oldVersion) {
			case 2: // current version
			case 3: // next version
				sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.User.NAME);
				sqLiteDatabase.execSQL(Table.User.CREATE_SQL);
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
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.AppEntry.NAME);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.Comment.NAME);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.User.NAME);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.UserApplicationRelated.NAME);
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Table.Vote.NAME);
		onCreate(sqLiteDatabase);
	}
}