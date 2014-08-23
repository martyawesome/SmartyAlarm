package com.martyawesome.smartyalarm.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.martyawesome.smartyalarm.AlarmConstants;
import com.martyawesome.smartyalarm.AlarmObject;
import com.martyawesome.smartyalarm.ChallengeObject;

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
			+ AlarmConstants.COLUMN_NAME_ALARM_ENABLED + " BOOLEAN,"
			+ AlarmConstants.COLUMN_NAME_ALARM_SNOOZE + " BOOLEAN,"
			+ AlarmConstants.COLUMN_NAME_ALARM_SNOOZE_TIME + " INTEGER" + " )";

	private static final String SQL_CREATE_CHALLENGES = "CREATE TABLE "
			+ AlarmConstants.TABLE_NAME_CHALLENGES + " ("
			+ AlarmConstants.COLUMN_NAME_CHALLENGE_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
			+ AlarmConstants.COLUMN_NAME_CHALLENGE_NAME + " TEXT,"
			+ AlarmConstants.COLUMN_NAME_CHALLENGE_IS_ENABLED + " BOOLEAN"
			+ " )";

	private static final String SQL_DELETE_ALARM = "DROP TABLE IF EXISTS "
			+ AlarmConstants.TABLE_NAME;

	public AlarmDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ALARM);
		db.execSQL(SQL_CREATE_CHALLENGES);
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
		object.isOnSnooze = c.getInt(c
				.getColumnIndex(AlarmConstants.COLUMN_NAME_ALARM_SNOOZE)) == 0 ? false
				: true;
		object.snoozeTime = c.getInt(c
				.getColumnIndex(AlarmConstants.COLUMN_NAME_ALARM_SNOOZE)) == 0 ? 0
				: c.getInt(c
						.getColumnIndex(AlarmConstants.COLUMN_NAME_ALARM_SNOOZE_TIME));

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
		values.put(AlarmConstants.COLUMN_NAME_ALARM_ENABLED, object.isEnabled);
		values.put(AlarmConstants.COLUMN_NAME_ALARM_SNOOZE, object.isOnSnooze);
		if (object.isOnSnooze == true)
			values.put(AlarmConstants.COLUMN_NAME_ALARM_SNOOZE_TIME,
					object.snoozeTime);
		else
			values.put(AlarmConstants.COLUMN_NAME_ALARM_SNOOZE_TIME, 0);

		String repeatingDays = "";
		for (int i = 0; i < 7; ++i) {
			repeatingDays += object.getRepeatingDay(i) + ",";
		}
		values.put(AlarmConstants.COLUMN_NAME_ALARM_REPEAT_DAYS, repeatingDays);

		return values;
	}

	private ChallengeObject populateChallengeObject(Cursor c) {
		ChallengeObject object = new ChallengeObject();

		object.id = c.getInt(c
				.getColumnIndex(AlarmConstants.COLUMN_NAME_CHALLENGE_ID));
		object.name = c.getString(c
				.getColumnIndex(AlarmConstants.COLUMN_NAME_CHALLENGE_NAME));
		object.isEnabled = c
				.getInt(c
						.getColumnIndex(AlarmConstants.COLUMN_NAME_CHALLENGE_IS_ENABLED)) == 0 ? false
				: true;

		return object;
	}

	public void createAlarm(AlarmObject object) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = populateContent(object);
		db.insert(AlarmConstants.TABLE_NAME, null, values);
		db.close(); // Closing database connection
	}

	public void createChallenges(ChallengeObject[] challengeObject) {
		SQLiteDatabase db = this.getWritableDatabase();

		for (int i = 0; i < challengeObject.length; i++) {
			ContentValues values = new ContentValues();
			values.put(AlarmConstants.COLUMN_NAME_CHALLENGE_ID,
					challengeObject[i].id);
			values.put(AlarmConstants.COLUMN_NAME_CHALLENGE_NAME,
					challengeObject[i].name);
			values.put(AlarmConstants.COLUMN_NAME_CHALLENGE_IS_ENABLED,
					challengeObject[i].isEnabled);
			db.insert(AlarmConstants.TABLE_NAME_CHALLENGES, null, values);
		}

		db.close(); // Closing database connection
	}

	public AlarmObject getAlarm(long id) {
		SQLiteDatabase db = this.getReadableDatabase();

		String select = "SELECT * FROM " + AlarmConstants.TABLE_NAME
				+ " WHERE " + AlarmConstants.COLUMN_NAME_ALARM_ID + " = " + id;

		Cursor c = db.rawQuery(select, null);

		if (c.moveToNext()) {
			return populateObject(c);
		}

		db.close();
		return null;
	}
	
	public ChallengeObject getChallenge(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		String select = "SELECT * FROM " + AlarmConstants.TABLE_NAME_CHALLENGES
				+ " WHERE " + AlarmConstants.COLUMN_NAME_CHALLENGE_ID + " = " + id;

		Cursor c = db.rawQuery(select, null);

		if (c.moveToNext()) {
			return populateChallengeObject(c);
		}

		db.close();
		return null;
	}

	public void updateAlarm(AlarmObject object) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = populateContent(object);
		db.update(AlarmConstants.TABLE_NAME, values,
				AlarmConstants.COLUMN_NAME_ALARM_ID + " = ?",
				new String[] { String.valueOf(object.id) });
		db.close();
	}
	
	public void updateChallenge(ChallengeObject object) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(AlarmConstants.COLUMN_NAME_CHALLENGE_ID,
				object.id);
		values.put(AlarmConstants.COLUMN_NAME_CHALLENGE_NAME,
				object.name);
		values.put(AlarmConstants.COLUMN_NAME_CHALLENGE_IS_ENABLED,
				object.isEnabled);
		
		db.update(AlarmConstants.TABLE_NAME_CHALLENGES, values,
				AlarmConstants.COLUMN_NAME_CHALLENGE_ID + " = ?",
				new String[] { String.valueOf(object.id) });
		db.close();
	}

	public void deleteAlarm(long id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(AlarmConstants.TABLE_NAME,
				AlarmConstants.COLUMN_NAME_ALARM_ID + " = ?",
				new String[] { String.valueOf(id) });
		db.close();
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

		db.close();
		return null;
	}

	public List<ChallengeObject> getChallenges() {
		SQLiteDatabase db = this.getReadableDatabase();

		String select = "SELECT * FROM " + AlarmConstants.TABLE_NAME_CHALLENGES;

		Cursor c = db.rawQuery(select, null);

		List<ChallengeObject> challengeList = new ArrayList<ChallengeObject>();

		while (c.moveToNext()) {
			challengeList.add(populateChallengeObject(c));
		}

		if (!challengeList.isEmpty()) {
			return challengeList;
		}

		db.close();
		return null;
	}
	
	public List<ChallengeObject> getEnabledChallenges() {
		SQLiteDatabase db = this.getReadableDatabase();

		String select = "SELECT * FROM " + AlarmConstants.TABLE_NAME_CHALLENGES
				+ " WHERE " + AlarmConstants.COLUMN_NAME_CHALLENGE_IS_ENABLED + " = "
				+ 1;

		Cursor c = db.rawQuery(select, null);

		List<ChallengeObject> challengeList = new ArrayList<ChallengeObject>();

		while (c.moveToNext()) {
			challengeList.add(populateChallengeObject(c));
		}

		if (!challengeList.isEmpty()) {
			return challengeList;
		}

		db.close();
		return null;
	}

	public int getMaxId(String tableName) {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = null;

		if (tableName.equals(AlarmConstants.TABLE_NAME))
			query = "SELECT MAX(" + AlarmConstants.COLUMN_NAME_ALARM_ID
					+ ") AS" + AlarmConstants.COLUMN_NAME_ALARM_ID + " FROM "
					+ AlarmConstants.TABLE_NAME;
		else if(tableName.equals(AlarmConstants.TABLE_NAME_CHALLENGES))
			query = "SELECT MAX(" + AlarmConstants.COLUMN_NAME_CHALLENGE_ID
					+ ") AS" + AlarmConstants.COLUMN_NAME_CHALLENGE_ID + " FROM "
					+ AlarmConstants.TABLE_NAME_CHALLENGES;
		Cursor cursor = db.rawQuery(query, null);

		int id = 0;
		if (cursor.moveToFirst()) {
			do {
				id = cursor.getInt(0);
			} while (cursor.moveToNext());
		}
		db.close();
		return id;
	}

	public boolean checkIfAllAreEnabled() {
		SQLiteDatabase db = this.getReadableDatabase();

		String select = "SELECT * FROM " + AlarmConstants.TABLE_NAME
				+ " WHERE " + AlarmConstants.COLUMN_NAME_ALARM_ENABLED + " = "
				+ 1;

		Cursor c = db.rawQuery(select, null);

		if (c != null && c.getCount() > 0) {
			db.close();
			return true;
		} else {
			db.close();
			return false;
		}
	}

}