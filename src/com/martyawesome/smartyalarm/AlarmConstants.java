package com.martyawesome.smartyalarm;

import java.util.Random;

public final class AlarmConstants {

	//Database Values
	public static final String TABLE_NAME = "alarm";
	public static final String COLUMN_NAME_ALARM_ID = "id";
	public static final String COLUMN_NAME_ALARM_NAME = "name";
	public static final String COLUMN_NAME_ALARM_TIME_HOUR = "hour";
	public static final String COLUMN_NAME_ALARM_TIME_MINUTE = "minute";
	public static final String COLUMN_NAME_ALARM_REPEAT_DAYS = "days";
	public static final String COLUMN_NAME_ALARM_REPEAT_WEEKLY = "weekly";
	public static final String COLUMN_NAME_ALARM_TONE = "tone";
	public static final String COLUMN_NAME_ALARM_ENABLED = "isEnabled";
	public static final String COLUMN_NAME_ALARM_SNOOZE = "isOnSnooze";
	public static final String COLUMN_NAME_ALARM_SNOOZE_TIME = "snoozeTime";
	
	//Intent Constants
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String TIME_HOUR = "timeHour";
	public static final String TIME_MINUTE = "timeMinute";
	public static final String TONE = "tone";
	public static final String SNOOZE = "snooze";
	public static final String SNOOZE_TIME = "snoozeTime";


}