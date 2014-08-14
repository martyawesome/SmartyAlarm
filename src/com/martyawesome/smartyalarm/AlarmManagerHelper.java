package com.martyawesome.smartyalarm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmManagerHelper extends BroadcastReceiver {
 
    @Override
    public void onReceive(Context context, Intent intent) {
        setAlarms(context);
    }
 
    public static void setAlarms(Context context) {
 
    }
 
    public static void cancelAlarms(Context context) {
 
    }
 
    private static PendingIntent createPendingIntent(Context context, AlarmObject object) {
        return null;
    }
}