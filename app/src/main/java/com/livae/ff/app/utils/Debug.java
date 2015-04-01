package com.livae.ff.app.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Debug {

	private static final String LOG_TAG = "DEBUG_SOP";

	public static void print(Cursor c) {
		if (!c.isClosed()) {
			int position = c.getPosition();
			String[] columns = c.getColumnNames();
			String aux = c.getCount() + " \t";
			for (String column : columns) {
				aux += column + " \t";
			}
			Log.d(LOG_TAG, aux);
			if (c.moveToFirst()) {
				int index = 1;
				do {
					aux = index + " \t";
					index++;
					for (int i = 0; i < columns.length; i++) {
						aux += getCursorData(c, i) + " \t";
					}
					Log.d(LOG_TAG, aux);
				} while (c.moveToNext());
			}
			c.moveToPosition(position);
		}
	}

	private static String getCursorData(Cursor c, int pos) {
		switch (c.getType(pos)) {
			case Cursor.FIELD_TYPE_STRING:
				return c.getString(pos);
			case Cursor.FIELD_TYPE_INTEGER:
				return Long.toString(c.getLong(pos));
			case Cursor.FIELD_TYPE_FLOAT:
				return Double.toString(c.getDouble(pos));
			case Cursor.FIELD_TYPE_NULL:
				return "null";
		}
		return "";
	}

	public static void print(ContentValues values) {
		for (String key : values.keySet()) {
			Log.d(LOG_TAG, key + "=" + values.get(key));
		}
	}

	public static void print(String[] args) {
		String aux = "";
		if (args != null && args.length > 0) {
			for (String s : args) {
				aux += s + " ";
			}
		}
		Log.d(LOG_TAG, aux);
	}

	public static void print(String text) {
		Log.d(LOG_TAG, text);
	}

	public static void print(SQLiteDatabase ddbb) {
		String mySql = " SELECT * FROM sqlite_master WHERE type='table'";
		Cursor cursor = ddbb.rawQuery(mySql, null);
		print(cursor);
	}

}
