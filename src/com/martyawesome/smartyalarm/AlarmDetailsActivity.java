package com.martyawesome.smartyalarm;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class AlarmDetailsActivity extends Activity {

	private Alarm mAlarm;
	TimePicker mTimePicker;
	EditText mAlarmName;
	CheckBox mCheckBoxWeekly;
	CheckBox mCheckBoxSunday;
	CheckBox mCheckBoxMonday;
	CheckBox mCheckBoxTuesday;
	CheckBox mCheckBoxWednesday;
	CheckBox mCheckBoxThursday;
	CheckBox mCheckBoxFriday;
	CheckBox mCheckBoxSaturday;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle("Create New Alarm");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.add_alarm);

		mTimePicker = (TimePicker) findViewById(R.id.alarm_details_time_picker);
		mAlarmName = (EditText) findViewById(R.id.alarm_details_name);
		mCheckBoxWeekly = (CheckBox) findViewById(R.id.alarm_details_repeat_weekly);
		mCheckBoxSunday = (CheckBox) findViewById(R.id.alarm_details_repeat_sunday);
		mCheckBoxMonday = (CheckBox) findViewById(R.id.alarm_details_repeat_monday);
		mCheckBoxTuesday = (CheckBox) findViewById(R.id.alarm_details_repeat_tuesday);
		mCheckBoxWednesday = (CheckBox) findViewById(R.id.alarm_details_repeat_wednesday);
		mCheckBoxThursday = (CheckBox) findViewById(R.id.alarm_details_repeat_thursday);
		mCheckBoxFriday = (CheckBox) findViewById(R.id.alarm_details_repeat_friday);
		mCheckBoxSaturday = (CheckBox) findViewById(R.id.alarm_details_repeat_saturday);
		
		final LinearLayout ringToneContainer = (LinearLayout) findViewById(R.id.alarm_ringtone_container);
		ringToneContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						RingtoneManager.ACTION_RINGTONE_PICKER);
				startActivityForResult(intent, 1);
			}
		});

		mAlarm = new Alarm();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_alarm, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			break;
		}
		case R.id.action_save_alarm_details: {
			newAlarmValues();
			finish();
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1: {
				mAlarm.alarmTone = data
						.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

				TextView txtToneSelection = (TextView) findViewById(R.id.alarm_label_tone_selection);
				txtToneSelection.setText(RingtoneManager.getRingtone(this,
						mAlarm.alarmTone).getTitle(this));
				break;
			}
			default: {
				break;
			}
			}
		}
	}

	private void newAlarmValues() {		
		mAlarm.timeMinute = mTimePicker.getCurrentMinute().intValue();
		mAlarm.timeHour = mTimePicker.getCurrentHour().intValue();
		mAlarm.name = mAlarmName.getText().toString();
		mAlarm.repeatWeekly = mCheckBoxWeekly.isChecked();
		mAlarm.setRepeatingDay(Alarm.SUNDAY, mCheckBoxSunday.isChecked());
		mAlarm.setRepeatingDay(Alarm.MONDAY, mCheckBoxMonday.isChecked());
		mAlarm.setRepeatingDay(Alarm.TUESDAY, mCheckBoxTuesday.isChecked());
		mAlarm.setRepeatingDay(Alarm.WEDNESDAY, mCheckBoxWednesday.isChecked());
		mAlarm.setRepeatingDay(Alarm.THURSDAY, mCheckBoxThursday.isChecked());
		mAlarm.setRepeatingDay(Alarm.FRIDAY, mCheckBoxFriday.isChecked());
		mAlarm.setRepeatingDay(Alarm.SATURDAY, mCheckBoxSaturday.isChecked());
		mAlarm.isEnabled = true;
	}
}
