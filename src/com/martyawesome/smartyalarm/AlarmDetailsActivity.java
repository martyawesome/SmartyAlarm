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

	private Alarm alarm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle("Create New Alarm");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.add_alarm);

		final LinearLayout ringToneContainer = (LinearLayout) findViewById(R.id.alarm_ringtone_container);
		ringToneContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(
						RingtoneManager.ACTION_RINGTONE_PICKER);
				startActivityForResult(intent, 1);
			}
		});

		alarm = new Alarm();

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
			updateModelFromLayout();
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
				alarm.alarmTone = data
						.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

				TextView txtToneSelection = (TextView) findViewById(R.id.alarm_label_tone_selection);
				txtToneSelection.setText(RingtoneManager.getRingtone(this,
						alarm.alarmTone).getTitle(this));
				break;
			}
			default: {
				break;
			}
			}
		}
	}

	private void updateModelFromLayout() {

		TimePicker timePicker = (TimePicker) findViewById(R.id.alarm_details_time_picker);
		alarm.timeMinute = timePicker.getCurrentMinute().intValue();
		alarm.timeHour = timePicker.getCurrentHour().intValue();

		EditText edtName = (EditText) findViewById(R.id.alarm_details_name);
		alarm.name = edtName.getText().toString();

		CheckBox chkWeekly = (CheckBox) findViewById(R.id.alarm_details_repeat_weekly);
		alarm.repeatWeekly = chkWeekly.isChecked();

		CheckBox chkSunday = (CheckBox) findViewById(R.id.alarm_details_repeat_sunday);
		alarm.setRepeatingDay(Alarm.SUNDAY, chkSunday.isChecked());

		CheckBox chkMonday = (CheckBox) findViewById(R.id.alarm_details_repeat_monday);
		alarm.setRepeatingDay(Alarm.MONDAY, chkMonday.isChecked());

		CheckBox chkTuesday = (CheckBox) findViewById(R.id.alarm_details_repeat_tuesday);
		alarm
				.setRepeatingDay(Alarm.TUESDAY, chkTuesday.isChecked());

		CheckBox chkWednesday = (CheckBox) findViewById(R.id.alarm_details_repeat_wednesday);
		alarm.setRepeatingDay(Alarm.WEDNESDAY,
				chkWednesday.isChecked());

		CheckBox chkThursday = (CheckBox) findViewById(R.id.alarm_details_repeat_thursday);
		alarm.setRepeatingDay(Alarm.THURSDAY,
				chkThursday.isChecked());

		CheckBox chkFriday = (CheckBox) findViewById(R.id.alarm_details_repeat_friday);
		alarm.setRepeatingDay(Alarm.FRIDAY, chkFriday.isChecked());

		CheckBox chkSaturday = (CheckBox) findViewById(R.id.alarm_details_repeat_saturday);
		alarm.setRepeatingDay(Alarm.SATURDAY,
				chkSaturday.isChecked());

		alarm.isEnabled = true;
	}
}
