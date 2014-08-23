package com.martyawesome.smartyalarm.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.martyawesome.smartyalarm.AlarmConstants;
import com.martyawesome.smartyalarm.AlarmObject;
import com.martyawesome.smartyalarm.ChallengeObject;
import com.martyawesome.smartyalarm.R;
import com.martyawesome.smartyalarm.adapters.AlarmListAdapter;
import com.martyawesome.smartyalarm.database.AlarmDBHelper;
import com.martyawesome.smartyalarm.services.AlarmManagerHelper;

@SuppressLint("NewApi")
public class AlarmsActivity extends ListActivity {

	private AlarmListAdapter mAdapter;
	private AlarmDBHelper dbHelper = new AlarmDBHelper(this);
	private Context mContext;
	private NotificationManager mNotificationManager;
	AlarmObject mAlarmObject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarms);
		mContext = this;
		
		if(!(dbHelper.getMaxId(AlarmConstants.TABLE_NAME_CHALLENGES) > 0))
			initializeChallenges();
		
        int actionBarTitle = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitleView = (TextView) getWindow().findViewById(actionBarTitle);
        Typeface tf = Typeface.createFromAsset(getAssets(), AlarmConstants.APP_FONT_STYLE);
        actionBarTitleView.setTypeface(tf);
        
		mAdapter = new AlarmListAdapter(this, dbHelper.getAlarms());
		setListAdapter(mAdapter);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alarms, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_alarm: {
			startAlarmDetailsActivity(0);
			break;
		}
		case R.id.settings: {
			Intent intent = new Intent(AlarmsActivity.this,
					SettingsActivity.class);
			startActivity(intent);
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	public void setAlarmEnabled(long id, boolean isEnabled) {
		AlarmManagerHelper.cancelAlarms(this);

		mAlarmObject = dbHelper.getAlarm(id);
		mAlarmObject.isEnabled = isEnabled;
		dbHelper.updateAlarm(mAlarmObject);

		mAdapter.setAlarms(dbHelper.getAlarms());
		mAdapter.notifyDataSetChanged();

		notification();

		AlarmManagerHelper.setAlarms(this);
	}

	public void startAlarmDetailsActivity(long id) {
		Intent intent = new Intent(AlarmsActivity.this,
				AlarmDetailsActivity.class);
		intent.putExtra("id", id);
		startActivityForResult(intent, 0);
	}

	public void deleteAlarm(long id) {
		final long alarmId = id;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please confirm").setTitle("Delete set?")
				.setCancelable(true).setNegativeButton("Cancel", null)
				.setPositiveButton("Ok", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Cancel Alarms
						AlarmManagerHelper.cancelAlarms(mContext);
						// Delete alarm from DB by id
						dbHelper.deleteAlarm(alarmId);
						// Refresh the list of the alarms in the adaptor
						mAdapter.setAlarms(dbHelper.getAlarms());
						// Notify the adapter the data has changed
						mAdapter.notifyDataSetChanged();

						if (dbHelper.getMaxId(AlarmConstants.TABLE_NAME) > 0) {
							// Set the alarms
							AlarmManagerHelper.setAlarms(mContext);
						}

					}
				}).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			mAdapter.setAlarms(dbHelper.getAlarms());
			mAdapter.notifyDataSetChanged();
		}
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
	
	public void initializeChallenges(){
		ChallengeObject[] listOfChallenges = new ChallengeObject[4];
				
		for(int i = 0; i<listOfChallenges.length; i++){
			listOfChallenges[i] = new ChallengeObject();
			listOfChallenges[i].isEnabled = true;
			listOfChallenges[i].id = i + 1;
		}
		
		listOfChallenges[0].name = getResources().getString(R.string.taptap);
		listOfChallenges[1].name = getResources().getString(R.string.math);
		listOfChallenges[2].name = getResources().getString(R.string.word);
		listOfChallenges[3].name = getResources().getString(R.string.wakeathon);
		
		dbHelper.createChallenges(listOfChallenges);
	}
}
