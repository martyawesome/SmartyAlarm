package com.martyawesome.smartyalarm;

import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmManagerHelper extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		setAlarms(context);
	}

	public static void setAlarms(Context context) {
		cancelAlarms(context);
		AlarmDBHelper dbHelper = new AlarmDBHelper(context);

		List<AlarmObject> alarms = dbHelper.getAlarms();

		for (AlarmObject alarm : alarms) {
			if (alarm.isEnabled) {
				PendingIntent pIntent = createPendingIntent(context, alarm);

				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, alarm.timeHour);
				calendar.set(Calendar.MINUTE, alarm.timeMinute);
				calendar.set(Calendar.SECOND, 00);

				// Find next time to set
				final int nowDay = Calendar.getInstance().get(
						Calendar.DAY_OF_WEEK);
				final int nowHour = Calendar.getInstance().get(
						Calendar.HOUR_OF_DAY);
				final int nowMinute = Calendar.getInstance().get(
						Calendar.MINUTE);
				boolean alarmSet = false;

				// First check if it's later in the week
				for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
					if (alarm.getRepeatingDay(dayOfWeek - 1)
							&& dayOfWeek >= nowDay
							&& !(dayOfWeek == nowDay && alarm.timeHour < nowHour)
							&& !(dayOfWeek == nowDay
									&& alarm.timeHour == nowHour && alarm.timeMinute <= nowMinute)) {
						calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);

						setAlarm(context, calendar, pIntent);
						alarmSet = true;
						break;
					}
				}

				// Else check if it's earlier in the week
				if (!alarmSet) {
					for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
						if (alarm.getRepeatingDay(dayOfWeek - 1)
								&& dayOfWeek <= nowDay && alarm.repeatWeekly) {
							calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
							calendar.add(Calendar.WEEK_OF_YEAR, 1);

							setAlarm(context, calendar, pIntent);
							alarmSet = true;
							break;
						}
					}
				}

			}
		}

	}

    @SuppressLint("NewApi")
    private static void setAlarm(Context context, Calendar calendar, PendingIntent pIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
        }
    }
    
    
	public static void cancelAlarms(Context context) {
		AlarmDBHelper dbHelper = new AlarmDBHelper(context);

		List<AlarmObject> alarms = dbHelper.getAlarms();

		if (alarms != null) {
			for (AlarmObject alarm : alarms) {
				if (alarm.isEnabled) {
					PendingIntent pIntent = createPendingIntent(context, alarm);

					AlarmManager alarmManager = (AlarmManager) context
							.getSystemService(Context.ALARM_SERVICE);
					alarmManager.cancel(pIntent);
				}
			}
		}
	}

	private static PendingIntent createPendingIntent(Context context,
			AlarmObject object) {

		Intent intent = new Intent(context, AlarmService.class);
		intent.putExtra(AlarmConstants.ID, object.id);
		intent.putExtra(AlarmConstants.NAME, object.name);
		intent.putExtra(AlarmConstants.TIME_HOUR, object.timeHour);
		intent.putExtra(AlarmConstants.TIME_MINUTE, object.timeMinute);
		intent.putExtra(AlarmConstants.TONE, object.alarmTone);

		return PendingIntent.getService(context, (int) object.id, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

	}
}