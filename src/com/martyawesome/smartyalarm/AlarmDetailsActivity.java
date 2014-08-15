package com.martyawesome.smartyalarm;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AlarmDetailsActivity extends Activity {

	private AlarmDBHelper dbHelper = new AlarmDBHelper(this);
	private AlarmObject mAlarmObject;
	TimePicker mTimePicker;
	EditText mAlarmName;
	CustomSwitch mCustomSwitchWeekly;
	CustomSwitch mCustomSwitchSunday;
	CustomSwitch mCustomSwitchMonday;
	CustomSwitch mCustomSwitchTuesday;
	CustomSwitch mCustomSwitchWednesday;
	CustomSwitch mCustomSwitchThursday;
	CustomSwitch mCustomSwitchFriday;
	CustomSwitch mCustomSwitchSaturday;
	TextView mToneSelection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle("Create New Alarm");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.add_alarm);

		mTimePicker = (TimePicker) findViewById(R.id.alarm_details_time_picker);
		mAlarmName = (EditText) findViewById(R.id.alarm_details_name);
		mCustomSwitchWeekly = (CustomSwitch) findViewById(R.id.alarm_details_repeat_weekly);
		mCustomSwitchSunday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_sunday);
		mCustomSwitchMonday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_monday);
		mCustomSwitchTuesday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_tuesday);
		mCustomSwitchWednesday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_wednesday);
		mCustomSwitchThursday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_thursday);
		mCustomSwitchFriday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_friday);
		mCustomSwitchSaturday = (CustomSwitch) findViewById(R.id.alarm_details_repeat_saturday);
		mToneSelection = (TextView) findViewById(R.id.alarm_label_tone_selection);

		long id = getIntent().getExtras().getLong("id");

		if (id <= 0) {
			mAlarmObject = new AlarmObject();
		} else {
			mAlarmObject = dbHelper.getAlarm(id);

			mTimePicker.setIs24HourView(false);
			mTimePicker.setCurrentMinute(mAlarmObject.timeMinute);
			mTimePicker.setCurrentHour(mAlarmObject.timeHour);

			mAlarmName.setText(mAlarmObject.name);

			mCustomSwitchWeekly.setChecked(mAlarmObject.repeatWeekly);
			mCustomSwitchSunday.setChecked(mAlarmObject
					.getRepeatingDay(AlarmObject.SUNDAY));
			mCustomSwitchMonday.setChecked(mAlarmObject
					.getRepeatingDay(AlarmObject.MONDAY));
			mCustomSwitchTuesday.setChecked(mAlarmObject
					.getRepeatingDay(AlarmObject.TUESDAY));
			mCustomSwitchWednesday.setChecked(mAlarmObject
					.getRepeatingDay(AlarmObject.WEDNESDAY));
			mCustomSwitchThursday.setChecked(mAlarmObject
					.getRepeatingDay(AlarmObject.THURSDAY));
			mCustomSwitchFriday.setChecked(mAlarmObject
					.getRepeatingDay(AlarmObject.FRIDAY));
			mCustomSwitchSaturday.setChecked(mAlarmObject
					.getRepeatingDay(AlarmObject.SATURDAY));

			if (mAlarmObject.alarmTone.toString() == ""
					|| mAlarmObject.alarmTone.toString().equals("")
					|| mAlarmObject.alarmTone.toString().equalsIgnoreCase(""))
				mToneSelection.setText(getResources().getString(
						R.string.details_alarm_tone_default));
			else
				mToneSelection.setText(RingtoneManager.getRingtone(this,
						mAlarmObject.alarmTone).getTitle(this));
		}

		final LinearLayout ringToneContainer = (LinearLayout) findViewById(R.id.alarm_ringtone_container);
		ringToneContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						RingtoneManager.ACTION_RINGTONE_PICKER);
				startActivityForResult(intent, 1);
			}
		});
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
			AlarmDBHelper dbHelper = new AlarmDBHelper(this);
			AlarmManagerHelper.cancelAlarms(this);

			if (mAlarmObject.id <= 0) {
				mAlarmObject.id = dbHelper.getMaxId() + 1;
				dbHelper.createAlarm(mAlarmObject);
				Toast.makeText(
						AlarmDetailsActivity.this,
						String.valueOf(getResources().getString(
								R.string.add_alarm_success)), Toast.LENGTH_LONG)
						.show();

			} else {
				dbHelper.updateAlarm(mAlarmObject);
				Toast.makeText(
						AlarmDetailsActivity.this,
						getResources().getString(R.string.update_alarm_success),
						Toast.LENGTH_LONG).show();

			}

			AlarmManagerHelper.setAlarms(this);
			setResult(RESULT_OK);
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
				// get URI data from intent sent
				mAlarmObject.alarmTone = data
						.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				mToneSelection.setText(RingtoneManager.getRingtone(this,
						mAlarmObject.alarmTone).getTitle(this));
				break;
			}
			default: {
				break;
			}
			}
		}
	}

	private void newAlarmValues() {
		mAlarmObject.timeMinute = mTimePicker.getCurrentMinute().intValue();
		mAlarmObject.timeHour = mTimePicker.getCurrentHour().intValue();

		if (mAlarmName.getText().toString() == null
				|| mAlarmName.getText().toString().equalsIgnoreCase(""))
			mAlarmObject.name = getResources().getString(R.string.untitled);
		else
			mAlarmObject.name = mAlarmName.getText().toString();

		mAlarmObject.repeatWeekly = mCustomSwitchWeekly.isChecked();
		mAlarmObject.setRepeatingDay(AlarmObject.SUNDAY,
				mCustomSwitchSunday.isChecked());
		mAlarmObject.setRepeatingDay(AlarmObject.MONDAY,
				mCustomSwitchMonday.isChecked());
		mAlarmObject.setRepeatingDay(AlarmObject.TUESDAY,
				mCustomSwitchTuesday.isChecked());
		mAlarmObject.setRepeatingDay(AlarmObject.WEDNESDAY,
				mCustomSwitchWednesday.isChecked());
		mAlarmObject.setRepeatingDay(AlarmObject.THURSDAY,
				mCustomSwitchThursday.isChecked());
		mAlarmObject.setRepeatingDay(AlarmObject.FRIDAY,
				mCustomSwitchFriday.isChecked());
		mAlarmObject.setRepeatingDay(AlarmObject.SATURDAY,
				mCustomSwitchSaturday.isChecked());
		
		if (mAlarmObject.alarmTone == null
				|| mAlarmObject.alarmTone.toString() == ""){
			mAlarmObject.alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		}
			
			mAlarmObject.isEnabled = true;
	}
}
