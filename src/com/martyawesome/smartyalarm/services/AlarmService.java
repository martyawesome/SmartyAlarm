package com.martyawesome.smartyalarm.services;

import com.martyawesome.smartyalarm.activities.AlarmScreenActivity;
import com.martyawesome.smartyalarm.activities.AlarmScreenCircleActivity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AlarmService extends Service {
 
    public static String TAG = AlarmService.class.getSimpleName();
 
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
 
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
   // 	Intent[] randomIntent = new Intent[2];
    //	randomIntent[0] = new Intent(getBaseContext(), AlarmScreenActivity.class);
    	
    	Intent alarmIntent = new Intent(getBaseContext(), AlarmScreenCircleActivity.class);
		alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		alarmIntent.putExtras(intent);
		getApplication().startActivity(alarmIntent);
		
		AlarmManagerHelper.setAlarms(this);
		
		return super.onStartCommand(intent, flags, startId);
    }
}