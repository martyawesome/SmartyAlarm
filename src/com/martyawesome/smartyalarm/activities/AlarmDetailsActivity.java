package com.martyawesome.smartyalarm.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.martyawesome.smartyalarm.AlarmConstants;
import com.martyawesome.smartyalarm.AlarmObject;
import com.martyawesome.smartyalarm.R;
import com.martyawesome.smartyalarm.database.AlarmDBHelper;
import com.martyawesome.smartyalarm.services.AlarmManagerHelper;
import com.martyawesome.smartyalarm.ui.CustomSwitch;

@SuppressLint("NewApi")
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
	CheckBox mSnooze;
	TextView mToneSelection;
	TextView mSnoozeMinutes;
	private NotificationManager mNotificationManager;
	int mSnoozeTime;
	SeekBar mSeekBar;
	LinearLayout mSnoozeLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		mSnoozeLayout = (LinearLayout) findViewById(R.id.snooze_container);

		mCustomSwitchWeekly.setChecked(true);
		mCustomSwitchSunday.setChecked(true);
		mCustomSwitchMonday.setChecked(true);
		mCustomSwitchTuesday.setChecked(true);
		mCustomSwitchWednesday.setChecked(true);
		mCustomSwitchThursday.setChecked(true);
		mCustomSwitchFriday.setChecked(true);
		mCustomSwitchSaturday.setChecked(true);

		int actionBarTitle = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");
		TextView actionBarTitleView = (TextView) getWindow().findViewById(
				actionBarTitle);
		Typeface tf = Typeface.createFromAsset(getAssets(),
				AlarmConstants.APP_FONT_STYLE);
		actionBarTitleView.setTypeface(tf);
		mAlarmName.setTypeface(tf);

		TextView alarmLabelTone = (TextView) findViewById(R.id.alarm_label_tone);
		alarmLabelTone.setTypeface(tf);
		mToneSelection.setTypeface(tf);
		
		mSnooze = (CheckBox) findViewById(R.id.snooze);
		mSnooze.setTypeface(tf);
		addSnoozeLayout(15);

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

			mSnooze.setChecked(mAlarmObject.isOnSnooze);
			if (mAlarmObject.isOnSnooze) {
				addSnoozeLayout(mAlarmObject.snoozeTime);
				seekBarListener(mAlarmObject.snoozeTime);
			}

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
				mAlarmObject.id = dbHelper.getMaxId(AlarmConstants.TABLE_NAME) + 1;
				dbHelper.createAlarm(mAlarmObject);
				Toast.makeText(
						AlarmDetailsActivity.this,
						String.valueOf(getResources().getString(
								R.string.add_alarm_success)), Toast.LENGTH_LONG)
						.show();

				notification();

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
				|| mAlarmObject.alarmTone.toString() == "") {
			mAlarmObject.alarmTone = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_ALARM);
		}

		mAlarmObject.isEnabled = true;

		if (mAlarmObject.isOnSnooze)
			mAlarmObject.snoozeTime = mSnoozeTime;
	}

	private void addSnoozeLayout(final int min) {
		mSeekBar = new SeekBar(this);
		mSnoozeMinutes = new TextView(this);
		mSnoozeMinutes.setTextSize(18);

		if (mSnooze.isChecked()) {
			mSnoozeTime = min;
		}

		mSnooze.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mAlarmObject.isOnSnooze = mSnooze.isChecked();

				if (mSnooze.isChecked()) {
					mSnoozeTime = min;
					seekBarListener(min);

				} else {
					mSnoozeLayout.removeView(mSeekBar);
					mSnoozeLayout.removeView(mSnoozeMinutes);
				}
			}

		});
	}

	private void seekBarListener(final int min) {

		mSnoozeLayout.addView(mSnoozeMinutes);
		mSeekBar.setMax(60);
		mSeekBar.setProgress(min);

		mSnoozeMinutes.setText(String.valueOf(min) + " min");
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (progress == 0)
					progress = 1;
				mSnoozeMinutes.setText(Integer.toString(progress) + " min");
				mSnoozeTime = progress;
			}
		});

		mSnoozeLayout.addView(mSeekBar);

	}

	@SuppressLint("NewApi")
	private void notification() {
		NotificationCompat.Builder mBuilder;

		if (mAlarmObject.timeHour > 12) {
			mBuilder = new NotificationCompat.Builder(this)
					.setLargeIcon(
							BitmapFactory.decodeResource(getResources(),
									R.drawable.ic_stat_alarm))
					.setSmallIcon(R.drawable.ic_stat_alarm)
					.setContentTitle(
							getResources().getString(R.string.notif_title))
					.setContentText(
							"Alarm set at "
									+ String.format(" %02d : %02d",
											mAlarmObject.timeHour - 12,
											mAlarmObject.timeMinute)
									+ " "
									+ getResources().getString(
											R.string.dayTimePM));

		} else if (mAlarmObject.timeHour < 12) {
			mBuilder = new NotificationCompat.Builder(this)
					.setLargeIcon(
							BitmapFactory.decodeResource(getResources(),
									R.drawable.ic_stat_alarm))
					.setSmallIcon(R.drawable.ic_stat_alarm)
					.setContentTitle(
							getResources().getString(R.string.notif_title))
					.setContentText(
							"Alarm set at "
									+ String.format(" %02d : %02d",
											mAlarmObject.timeHour,
											mAlarmObject.timeMinute)
									+ " "
									+ getResources().getString(
											R.string.dayTimeAM));
		} else {
			mBuilder = new NotificationCompat.Builder(this)
					.setLargeIcon(
							BitmapFactory.decodeResource(getResources(),
									R.drawable.ic_stat_alarm))
					.setSmallIcon(R.drawable.ic_stat_alarm)
					.setContentTitle(
							getResources().getString(R.string.notif_title))
					.setContentText(
							"Alarm set at "
									+ String.format(" %02d : %02d",
											mAlarmObject.timeHour,
											mAlarmObject.timeMinute)
									+ " "
									+ getResources().getString(
											R.string.dayTimePM));
		}

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		if (dbHelper.checkIfAllAreEnabled()) {

			mNotificationManager
					.notify((int) mAlarmObject.id, mBuilder.build());
		} else {
			mNotificationManager.cancelAll();
		}

	}
}
