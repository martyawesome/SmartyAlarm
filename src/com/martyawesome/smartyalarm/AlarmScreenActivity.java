package com.martyawesome.smartyalarm;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class AlarmScreenActivity extends Activity {

	public final String TAG = this.getClass().getSimpleName();

	private WakeLock mWakeLock;
	private MediaPlayer mPlayer;

	private static final int WAKELOCK_TIMEOUT = 60 * 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_alarm_screen);

		String name = getIntent().getStringExtra(AlarmConstants.NAME);
		int timeHour = getIntent().getIntExtra(AlarmConstants.TIME_HOUR, 0);
		int timeMinute = getIntent().getIntExtra(AlarmConstants.TIME_MINUTE, 0);
		String tone = getIntent().getStringExtra(AlarmConstants.TONE);

		TextView tvName = (TextView) findViewById(R.id.alarm_screen_name);
		tvName.setText(name);

		TextView tvTime = (TextView) findViewById(R.id.alarm_screen_time);
		tvTime.setText(String.format("%02d : %02d", timeHour, timeMinute));
		
		// Play alarm tone
				mPlayer = new MediaPlayer();
				try {
					if (tone != null && !tone.equals("")) {
						Uri toneUri = Uri.parse(tone);
						if (toneUri != null) {
							mPlayer.setDataSource(this, toneUri);
							mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
							mPlayer.setLooping(true);
							mPlayer.prepare();
							mPlayer.start();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

		Button dismissButton = (Button) findViewById(R.id.alarm_screen_button);
		dismissButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				mPlayer.stop();
				finish();
			}
		});

		

		// Ensure wakelock release
		Runnable releaseWakelock = new Runnable() {

			@Override
			public void run() {
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
				getWindow().clearFlags(
						WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

				if (mWakeLock != null && mWakeLock.isHeld()) {
					mWakeLock.release();
				}
			}
		};

		new Handler().postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);
	}
	
	@Override
    protected void onResume() {
        super.onResume();
 
        // Set the window to keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
 
        // Acquire wakelock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (mWakeLock == null) {
			mWakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK | PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), TAG);
		}

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
			//Log.i(TAG, "Wakelock aquired!!");
		}
 
    }
 
    @Override
    protected void onPause() {
        super.onPause();
 
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }
	
	
}
