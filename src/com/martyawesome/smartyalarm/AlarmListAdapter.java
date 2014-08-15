package com.martyawesome.smartyalarm;

import java.util.List;

import android.R.integer;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AlarmListAdapter extends BaseAdapter {

	private Context mContext;
	private List<AlarmObject> mAlarms;
	public AlarmObject mObject;

	public AlarmListAdapter(Context context, List<AlarmObject> alarms) {
		mContext = context;
		mAlarms = alarms;
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
		txtTime.setText(String.format("%02d : %02d", mObject.timeHour,
				mObject.timeMinute));

		TextView txtName = (TextView) view.findViewById(R.id.alarm_item_name);
		txtName.setText(mObject.name);

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

		ToggleButton btnToggle = (ToggleButton) view.findViewById(R.id.alarm_item_toggle);
		btnToggle.setChecked(mObject.isEnabled);

		btnToggle.setTag(Long.valueOf(mObject.id));
		btnToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				((AlarmsActivity) mContext).setAlarmEnabled(((Long) buttonView.getTag()).longValue(), isChecked);
			}
		});
		
		view.setTag(Long.valueOf(mObject.id));
	    view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				((AlarmsActivity) mContext).startAlarmDetailsActivity(((Long) view.getTag()).longValue());
			}
		});
		return view;
	}

	private void updateTextColor(TextView view, boolean isOn) {
		if (isOn) {
			view.setTextColor(Color.GREEN);
		} else {
			view.setTextColor(Color.BLACK);
		}
	}

}
