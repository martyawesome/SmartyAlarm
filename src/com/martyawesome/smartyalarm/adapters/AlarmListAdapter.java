package com.martyawesome.smartyalarm.adapters;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.martyawesome.smartyalarm.AlarmConstants;
import com.martyawesome.smartyalarm.AlarmObject;
import com.martyawesome.smartyalarm.R;
import com.martyawesome.smartyalarm.activities.AlarmsActivity;

public class AlarmListAdapter extends BaseAdapter {

	private Context mContext;
	private List<AlarmObject> mAlarms;
	public AlarmObject mObject;
	private Typeface tf;

	public AlarmListAdapter(Context context, List<AlarmObject> alarms) {
		mContext = context;
		mAlarms = alarms;

		int actionBarTitle = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");
		TextView actionBarTitleView = (TextView) ((Activity) context)
				.getWindow().findViewById(actionBarTitle);
		tf = Typeface.createFromAsset(context.getAssets(),
				AlarmConstants.APP_FONT_STYLE);
		actionBarTitleView.setTypeface(tf);
	}

	public void setAlarms(List<AlarmObject> alarms) {
		mAlarms = alarms;
	}

	@Override
	public int getCount() {
		if (mAlarms != null) {
			return mAlarms.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mAlarms != null) {
			return mAlarms.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (mAlarms != null) {
			return mAlarms.get(position).id;
		}
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {

		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.alarm_list_item, parent, false);
		}

		mObject = (AlarmObject) getItem(position);
		TextView txtTime = (TextView) view.findViewById(R.id.alarm_item_time);
		txtTime.setTypeface(tf);
		TextView txtDayFormat = (TextView) view
				.findViewById(R.id.alarm_item_time_day);
		txtDayFormat.setTypeface(tf);

		if (mObject.timeHour > 12) {
			txtTime.setText(String.format("%02d : %02d", mObject.timeHour - 12,
					mObject.timeMinute));
			txtDayFormat.setText(mContext.getResources().getString(
					R.string.dayTimePM));
		} else if (mObject.timeHour < 12) {
			txtTime.setText(String.format("%02d : %02d", mObject.timeHour,
					mObject.timeMinute));
			txtDayFormat.setText(mContext.getResources().getString(
					R.string.dayTimeAM));
		} else {
			txtTime.setText(String.format("%02d : %02d", mObject.timeHour,
					mObject.timeMinute));
			txtDayFormat.setText(mContext.getResources().getString(
					R.string.dayTimePM));
		}

		TextView txtName = (TextView) view.findViewById(R.id.alarm_item_name);
		txtName.setText(mObject.name);
		txtName.setTypeface(tf);

		updateTextColor((TextView) view.findViewById(R.id.alarm_item_sunday),
				mObject.getRepeatingDay(AlarmObject.SUNDAY));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_monday),
				mObject.getRepeatingDay(AlarmObject.MONDAY));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_tuesday),
				mObject.getRepeatingDay(AlarmObject.TUESDAY));
		updateTextColor(
				(TextView) view.findViewById(R.id.alarm_item_wednesday),
				mObject.getRepeatingDay(AlarmObject.WEDNESDAY));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_thursday),
				mObject.getRepeatingDay(AlarmObject.THURSDAY));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_friday),
				mObject.getRepeatingDay(AlarmObject.FRIDAY));
		updateTextColor((TextView) view.findViewById(R.id.alarm_item_saturday),
				mObject.getRepeatingDay(AlarmObject.SATURDAY));

		TextView txtSnooze = (TextView) view.findViewById(R.id.alarm_snooze);
		txtSnooze.setTypeface(tf);
		if (mObject.isOnSnooze)
			if (mObject.snoozeTime > 1)
				txtSnooze.setText(mContext.getResources().getString(
						R.string.snooze_adapter)
						+ " Every "
						+ String.valueOf(mObject.snoozeTime)
						+ " minute");
			else
				txtSnooze.setText(mContext.getResources().getString(
						R.string.snooze_adapter)
						+ " Every "
						+ String.valueOf(mObject.snoozeTime)
						+ " minute");
		else
			txtSnooze.setText(mContext.getResources().getString(
					R.string.snooze_adapter)
					+ " Off");

		TextView txtTimeLeft = (TextView) view
				.findViewById(R.id.alarm_time_left);
		txtTimeLeft.setTypeface(tf);

		if (mObject.isEnabled) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, mObject.timeHour);
			calendar.set(Calendar.MINUTE, mObject.timeMinute);

			// Find next time to set
			final int nowDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
			final int nowHour = Calendar.getInstance()
					.get(Calendar.HOUR_OF_DAY);
			final int nowMinute = Calendar.getInstance().get(Calendar.MINUTE);

			Calendar calendarCurrent = Calendar.getInstance();
			calendarCurrent.set(Calendar.HOUR_OF_DAY, nowHour);
			calendarCurrent.set(Calendar.MINUTE, nowMinute);
			calendarCurrent.set(Calendar.DAY_OF_WEEK, nowDay);

			boolean alarmSet = false;
			boolean weeklyAlarmEnabled = true;

			// First check if it's later in the week
			for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
				if (mObject.getRepeatingDay(dayOfWeek - 1)
						&& dayOfWeek >= nowDay
						&& !(dayOfWeek == nowDay && mObject.timeHour < nowHour)
						&& !(dayOfWeek == nowDay && mObject.timeHour == nowHour && mObject.timeMinute <= nowMinute)) {
					calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
					alarmSet = true;
					break;
				}
			}

			// Else check if it's earlier in the week
			if (!alarmSet) {
				for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; ++dayOfWeek) {
					if (mObject.getRepeatingDay(dayOfWeek - 1)
							&& dayOfWeek <= nowDay && mObject.repeatWeekly) {
						calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
						calendar.add(Calendar.WEEK_OF_YEAR, 1);
						alarmSet = true;
						break;
					}
					if (mObject.getRepeatingDay(dayOfWeek - 1)
							&& dayOfWeek <= nowDay && !mObject.repeatWeekly) {
						calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
						calendar.add(Calendar.WEEK_OF_YEAR, 1);
						weeklyAlarmEnabled = false;
						break;
					}
				}
			}

			// get Time in milli seconds
			long ms1 = calendar.getTimeInMillis();
			long ms2 = calendarCurrent.getTimeInMillis();
			// get difference in milli seconds
			long diff = ms1 - ms2;
			int diffInSec = (int) (diff / (1000));
			int diffInMin = (int) (diff / (60 * 1000));
			int diffInHour = (int) (diff / (60 * 60 * 1000));
			int diffInDays = (int) (diff / (24 * 60 * 60 * 1000));

			String nextAlarm = "In";

			if (diffInSec > 0) {
				if (diffInSec >= 60)
					if (diffInMin >= 60)
						if (diffInHour >= 24)
							nextAlarm += " " + String.valueOf(diffInDays)
									+ " days";
						else {
							nextAlarm += " " + String.valueOf(diffInHour)
									+ " hours";
							if (diffInMin % 60 > 0 && diffInMin % 60 < 60)
								nextAlarm += " "
										+ String.valueOf(diffInMin % 60)
										+ " min";
							if (diffInSec % 60 > 0 && diffInSec % 60 < 60)
								nextAlarm += " "
										+ String.valueOf(diffInSec % 60)
										+ " sec";
						}
					else {
						nextAlarm += " " + String.valueOf(diffInMin) + " min";
						if (diffInSec % 60 > 0 && diffInSec % 60 < 60)
							nextAlarm += " " + String.valueOf(diffInSec % 60)
									+ " sec";
					}
				else
					nextAlarm += " " + String.valueOf(diffInSec) + " sec";
			} else
				nextAlarm = "No Day is Activated";

			if (!weeklyAlarmEnabled)
				nextAlarm = "Repeat weekly is not on";

			txtTimeLeft.setText(nextAlarm);

		} else
			txtTimeLeft.setText("Disabled");

		ToggleButton btnToggle = (ToggleButton) view
				.findViewById(R.id.alarm_item_toggle);
		btnToggle.setChecked(mObject.isEnabled);
		btnToggle.setTypeface(tf);

		btnToggle.setTag(Long.valueOf(mObject.id));
		btnToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				((AlarmsActivity) mContext).setAlarmEnabled(
						((Long) buttonView.getTag()).longValue(), isChecked);
			}
		});

		view.setTag(Long.valueOf(mObject.id));
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				((AlarmsActivity) mContext)
						.startAlarmDetailsActivity(((Long) view.getTag())
								.longValue());
			}
		});

		view.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View view) {
				((AlarmsActivity) mContext).deleteAlarm(((Long) view.getTag())
						.longValue());
				return true;
			}
		});

		return view;
	}

	private void updateTextColor(TextView view, boolean isOn) {
		if (isOn) {
			view.setTextColor(mContext.getResources().getColor(
					R.color.enabledDay));
		} else {
			view.setTextColor(Color.BLACK);
		}
		
		view.setTypeface(tf);
	}

}
