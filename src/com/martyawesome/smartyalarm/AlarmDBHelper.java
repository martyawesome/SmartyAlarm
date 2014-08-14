package com.martyawesome.smartyalarm;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class AlarmDBHelper extends SQLiteOpenHelper {

	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "alarmclock.db";

	private static final String SQL_CREATE_ALARM = "CREATE TABLE "
			+ AlarmConstants.TABLE_NAME + " ("
			+ AlarmConstants.COLUMN_NAME_ALARM_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
			+ AlarmConstants.COLUMN_NAME_ALARM_NAME + " TEXT,"
			+ AlarmConstants.COLUMN_NAME_ALARM_TIME_HOUR + " INTEGER,"
			+ AlarmConstants.COLUMN_NAME_ALARM_TIME_MINUTE + " INTEGER,"
			+ AlarmConstants.COLUMN_NAME_ALARM_REPEAT_DAYS + " TEXT,"
			+ AlarmConstants.COLUMN_NAME_ALARM_REPEAT_WEEKLY + " BOOLEAN,"
			+ AlarmConstants.COLUMN_NAME_ALARM_TONE + " TEXT,"
			+ AlarmConstants.COLUMN_NAME_ALARM_ENABLED + " BOOLEAN" + " )";

	private static final String SQL_DELETE_ALARM = "DROP TABLE IF EXISTS "
			+ AlarmConstants.TABLE_NAME;

	public AlarmDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ALARM);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DELETE_ALARM);
		onCreate(db);
	}

	private AlarmObject populateObject(Cursor c) {
		AlarmObject object = new AlarmObject();
		object.id = c.getLong(c
				.getColumnIndex(AlarmConstants.COLUMN_NAME_ALARM_ID));
		object.name = c.getString(c
				.getColumnIndex(AlarmConstants.COLUMN_NAME_ALARM_NAME));
		object.timeHour = c.getInt(c
				.getColumnIndex(AlarmConstants.COLUMN_NAME_ALARM_TIME_HOUR));
		object.timeMinute = c.getInt(c
				.getColumnIndex(AlarmConstants.COLUMN_NAME_ALARM_TIME_MINUTE));
		object.repeatWeekly = c
				.getInt(c
						.getColumnIndex(AlarmConstants.COLUMN_NAME_ALARM_REPEAT_WEEKLY)) == 0 ? false
				: true;
		object.alarmTone = c.getString(c
				.getColumnIndex(AlarmConstants.COLUMN_NAME_ALARM_TONE)) != "" ? Uri
				.parse(c.getString(c
						.getColumnIndex(AlarmConstants.COLUMN_NAME_ALARM_TONE)))
				: null;
		object.isEnabled = c.getInt(c
				.getColumnIndex(AlarmConstants.COLUMN_NAME_ALARM_ENABLED)) == 0 ? false
				: true;
		String[] repeatingDays = c.getString(
				c.getColumnIndex(AlarmConstants.COLUMN_NAME_ALARM_REPEAT_DAYS))
				.split(",");
		for (int i = 0; i < repeatingDays.length; ++i) {
			object.setRepeatingDay(i, repeatingDays[i].equals("false") ? false
					: true);
		}

		return object;
	}

	private ContentValues populateContent(AlarmObject object) {
		ContentValues values = new ContentValues();
		values.put(AlarmConstants.COLUMN_NAME_ALARM_ID, object.id);
		values.put(AlarmConstants.COLUMN_NAME_ALARM_NAME, object.name);
		values.put(AlarmConstants.COLUMN_NAME_ALARM_TIME_HOUR, object.timeHour);
		values.put(AlarmConstants.COLUMN_NAME_ALARM_TIME_MINUTE,
				object.timeMinute);
		values.put(AlarmConstants.COLUMN_NAME_ALARM_REPEAT_WEEKLY,
				object.repeatWeekly);
		values.put(AlarmConstants.COLUMN_NAME_ALARM_TONE,
				object.alarmTone != null ? object.alarmTone.toString() : "");

		String repeatingDays = "";
		for (int i = 0; i < 7; ++i) {
			repeatingDays += object.getRepeatingDay(i) + ",";
		}
		values.put(AlarmConstants.COLUMN_NAME_ALARM_REPEAT_DAYS, repeatingDays);

		return values;
	}

	public long createAlarm(AlarmObject object) {
		ContentValues values = populateContent(object);
		return getWritableDatabase().insert(AlarmConstants.TABLE_NAME, null,
				values);
	}

	public AlarmObject getAlarm(long id) {
		SQLiteDatabase db = this.getReadableDatabase();

		String select = "SELECT * FROM " + AlarmConstants.TABLE_NAME
				+ " WHERE " + AlarmConstants.COLUMN_NAME_ALARM_ID + " = " + id;

		Cursor c = db.rawQuery(select, null);

		if (c.moveToNext()) {
			return populateObject(c);
		}

		return null;
	}

	public long updateAlarm(AlarmObject object) {
		ContentValues values = populateContent(object);
		return getWritableDatabase().update(AlarmConstants.TABLE_NAME, values,
				AlarmConstants.COLUMN_NAME_ALARM_ID + " = ?",
				new String[] { String.valueOf(object.id) });
	}

	public int deleteAlarm(long id) {
		return getWritableDatabase().delete(AlarmConstants.TABLE_NAME,
				AlarmConstants.COLUMN_NAME_ALARM_ID + " = ?",
				new String[] { String.valueOf(id) });
	}

	public List<AlarmObject> getAlarms() {
		SQLiteDatabase db = this.getReadableDatabase();

		String select = "SELECT * FROM " + AlarmConstants.TABLE_NAME;

		Cursor c = db.rawQuery(select, null);

		List<AlarmObject> alarmList = new ArrayList<AlarmObject>();

		while (c.moveToNext()) {
			alarmList.add(populateObject(c));
		}

		if (!alarmList.isEmpty()) {
			return alarmList;
		}

		return null;
	}

	public int getMaxId() {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = null;

		query = "SELECT MAX(" + AlarmConstants.COLUMN_NAME_ALARM_ID + ") AS"
				+ AlarmConstants.COLUMN_NAME_ALARM_ID + " FROM "
				+ AlarmConstants.TABLE_NAME;
		Cursor cursor = db.rawQuery(query, null);

		int id = 0;
		if (cursor.moveToFirst()) {
			do {
				id = cursor.getInt(0);
			} while (cursor.moveToNext());
		}
		return id;
	}

}