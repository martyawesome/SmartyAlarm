package com.martyawesome.smartyalarm.activities;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.martyawesome.smartyalarm.AlarmConstants;
import com.martyawesome.smartyalarm.ChallengeObject;
import com.martyawesome.smartyalarm.R;
import com.martyawesome.smartyalarm.adapters.ChallengesListAdapter;
import com.martyawesome.smartyalarm.database.AlarmDBHelper;
import com.martyawesome.smartyalarm.services.AlarmManagerHelper;

public class SettingsActivity extends ListActivity {

	public ChallengesListAdapter mAdapter;
	private AlarmDBHelper dbHelper = new AlarmDBHelper(this);
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		mContext = this;

		mAdapter = new ChallengesListAdapter(this, dbHelper.getChallenges());
		setListAdapter(mAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alarms, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateChallenge(int id, boolean isEnabled) {
		ChallengeObject object = dbHelper.getChallenge(id);
		object.isEnabled = isEnabled;
		dbHelper.updateChallenge(object);
	}

}
