package com.martyawesome.smartyalarm.services;

import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.martyawesome.smartyalarm.ChallengeObject;
import com.martyawesome.smartyalarm.R;
import com.martyawesome.smartyalarm.activities.AlarmScreenCircleActivity;
import com.martyawesome.smartyalarm.activities.AlarmScreenMathActivity;
import com.martyawesome.smartyalarm.activities.AlarmScreenWakeathonActivity;
import com.martyawesome.smartyalarm.activities.AlarmScreenWordActivity;
import com.martyawesome.smartyalarm.database.AlarmDBHelper;

public class AlarmService extends Service {

	public static String TAG = AlarmService.class.getSimpleName();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		AlarmDBHelper dbHelper = new AlarmDBHelper(this);
		ChallengeObject[] objects = (dbHelper.getEnabledChallenges())
				.toArray(new ChallengeObject[dbHelper.getEnabledChallenges()
						.size()]);

		Intent[] intents = new Intent[dbHelper.getEnabledChallenges().size()];
		for (int i = 0; i < dbHelper.getEnabledChallenges().size(); i++) {

			if (objects[i].name.equals(getResources().getString(R.string.taptap)))
				intents[i] = new Intent(getBaseContext(),
						AlarmScreenCircleActivity.class);
			else if (objects[i].name.equals(getResources().getString(R.string.math)))
				intents[i] = new Intent(getBaseContext(),
						AlarmScreenMathActivity.class);
			else if (objects[i].name.equals(getResources().getString(R.string.word)))
				intents[i] = new Intent(getBaseContext(),
						AlarmScreenWordActivity.class);
			else if (objects[i].name.equals(getResources().getString(R.string.wakeathon)))
				intents[i] = new Intent(getBaseContext(),
						AlarmScreenWakeathonActivity.class);

		}

		Random random = new Random();
		int randomNum = random.nextInt(dbHelper.getEnabledChallenges().size());

		Intent intentPass = intents[randomNum];
		
		intentPass.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intentPass.putExtras(intent);
		getApplication().startActivity(intentPass);

		AlarmManagerHelper.setAlarms(this);

		return super.onStartCommand(intent, flags, startId);
	}
}