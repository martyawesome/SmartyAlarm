package com.martyawesome.smartyalarm.services;

import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.martyawesome.smartyalarm.activities.AlarmScreenCircleActivity;
import com.martyawesome.smartyalarm.activities.AlarmScreenMathActivity;

public class AlarmService extends Service {
 
    public static String TAG = AlarmService.class.getSimpleName();
 
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
 
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Intent[] randomIntent = new Intent[2];
    	randomIntent[0] = new Intent(getBaseContext(), AlarmScreenCircleActivity.class);
    	randomIntent[1] = new Intent(getBaseContext(), AlarmScreenMathActivity.class);
    	
    	Random random = new Random();
    	int randomNum = random.nextInt(randomIntent.length);
    	
		randomIntent[randomNum].addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		randomIntent[randomNum].putExtras(intent);
		getApplication().startActivity(randomIntent[randomNum]);
		
		AlarmManagerHelper.setAlarms(this);
		
		return super.onStartCommand(intent, flags, startId);
    }
}