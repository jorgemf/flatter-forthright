package com.livae.ff.app.utils;

import android.database.Cursor;
import android.support.annotation.NonNull;

public class CursorUtils {

	public static Integer getInt(@NonNull Cursor cursor, @NonNull String columnName) {
		Integer value = null;
		int index = cursor.getColumnIndex(columnName);
		if (index >= 0 && !cursor.isNull(index)) {
			value = cursor.getInt(index);
		}
		return value;
	}

	public static Long getLong(@NonNull Cursor cursor, @NonNull String columnName) {
		Long value = null;
		int index = cursor.getColumnIndex(columnName);
		if (index >= 0 && !cursor.isNull(index)) {
			value = cursor.getLong(index);
		}
		return value;
	}

	public static Double getDouble(@NonNull Cursor cursor, @NonNull String columnName) {
		Double value = null;
		int index = cursor.getColumnIndex(columnName);
		if (index >= 0 && !cursor.isNull(index)) {
			value = cursor.getDouble(index);
		}
		return value;
	}

	public static Boolean getBoolean(@NonNull Cursor cursor, @NonNull String columnName) {
		Boolean value = null;
		int index = cursor.getColumnIndex(columnName);
		if (index >= 0 && !cursor.isNull(index)) {
			value = cursor.getInt(index) != 0;
		}
		return value;
	}

	public static Float getFloat(@NonNull Cursor cursor, @NonNull String columnName) {
		Float value = null;
		int index = cursor.getColumnIndex(columnName);
		if (index >= 0 && !cursor.isNull(index)) {
			value = cursor.getFloat(index);
		}
		return value;
	}

	public static String getString(@NonNull Cursor cursor, @NonNull String columnName) {
		String value = null;
		int index = cursor.getColumnIndex(columnName);
		if (index >= 0 && !cursor.isNull(index)) {
			value = cursor.getString(index);
		}
		return value;
	}

	public static Integer getIntOrThrow(@NonNull Cursor cursor, @NonNull String columnName) {
		Integer value = null;
		int index = cursor.getColumnIndexOrThrow(columnName);
		if (!cursor.isNull(index)) {
			value = cursor.getInt(index);
		}
		return value;
	}

	public static Long getLongOrThrow(@NonNull Cursor cursor, @NonNull String columnName) {
		Long value = null;
		int index = cursor.getColumnIndexOrThrow(columnName);
		if (!cursor.isNull(index)) {
			value = cursor.getLong(index);
		}
		return value;
	}

	public static Double getDoubleOrThrow(@NonNull Cursor cursor, @NonNull String columnName) {
		Double value = null;
		int index = cursor.getColumnIndexOrThrow(columnName);
		if (!cursor.isNull(index)) {
			value = cursor.getDouble(index);
		}
		return value;
	}

	public static Boolean getBooleanOrThrow(@NonNull Cursor cursor, @NonNull String columnName) {
		Boolean value = null;
		int index = cursor.getColumnIndexOrThrow(columnName);
		if (!cursor.isNull(index)) {
			value = cursor.getInt(index) != 0;
		}
		return value;
	}

	public static Float getFloatOrThrow(@NonNull Cursor cursor, @NonNull String columnName) {
		Float value = null;
		int index = cursor.getColumnIndexOrThrow(columnName);
		if (!cursor.isNull(index)) {
			value = cursor.getFloat(index);
		}
		return value;
	}

	public static String getStringOrThrow(@NonNull Cursor cursor, @NonNull String columnName) {
		String value = null;
		int index = cursor.getColumnIndexOrThrow(columnName);
		if (!cursor.isNull(index)) {
			value = cursor.getString(index);
		}
		return value;
	}

}
