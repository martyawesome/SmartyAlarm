package com.martyawesome.smartyalarm;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class AlarmsActivity extends ListActivity {

	private AlarmListAdapter mAdapter;
	private AlarmDBHelper dbHelper = new AlarmDBHelper(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarms);

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
		}
		return super.onOptionsItemSelected(item);
	}

	public void setAlarmEnabled(long id, boolean isEnabled) {
		AlarmObject object = dbHelper.getAlarm(id);
		object.isEnabled = isEnabled;
		dbHelper.updateAlarm(object);

		mAdapter.setAlarms(dbHelper.getAlarms());
		mAdapter.notifyDataSetChanged();
	}

	public void startAlarmDetailsActivity(long id) {
		Intent intent = new Intent(AlarmsActivity.this, AlarmDetailsActivity.class);
		intent.putExtra("id", id);
		//Toast.makeText(AlarmsActivity.this, String.valueOf(id), Toast.LENGTH_SHORT).show();
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			mAdapter.setAlarms(dbHelper.getAlarms());
			mAdapter.notifyDataSetChanged();
		}
	}

}
