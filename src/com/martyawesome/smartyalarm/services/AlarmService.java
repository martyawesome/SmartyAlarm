package com.martyawesome.smartyalarm.services;

import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.martyawesome.smartyalarm.activities.AlarmScreenCircleActivity;
import com.martyawesome.smartyalarm.activities.AlarmScreenMathActivity;
import com.martyawesome.smartyalarm.activities.AlarmScreenWordActivity;

public class AlarmService extends Service {

	public static String TAG = AlarmService.class.getSimpleName();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Random random = new Random();
		int randomNum = random.nextInt(3);

		Intent intentPass = null;
		switch (randomNum) {
		case 0:
			intentPass = new Intent(getBaseContext(),
					AlarmScreenCircleActivity.class);
			break;
		case 1:
			intentPass = new Intent(getBaseContext(),
					AlarmScreenMathActivity.class);
			break;
		case 2:
			intentPass = new Intent(getBaseContext(),
					AlarmScreenWordActivity.class);
			break;
		}

		intentPass.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intentPass.putExtras(intent);
		getApplication().startActivity(intentPass);

		AlarmManagerHelper.setAlarms(this);

		return super.onStartCommand(intent, flags, startId);
	}
}