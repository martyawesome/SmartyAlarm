package com.martyawesome.smartyalarm.activities;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.martyawesome.smartyalarm.AlarmConstants;
import com.martyawesome.smartyalarm.AlarmObject;
import com.martyawesome.smartyalarm.R;
import com.martyawesome.smartyalarm.database.AlarmDBHelper;
import com.martyawesome.smartyalarm.services.AlarmManagerHelper;

public class AlarmScreenCircleActivity extends Activity {

	public final String TAG = this.getClass().getSimpleName();

	private WakeLock mWakeLock;
	private MediaPlayer mPlayer;

	private static final int WAKELOCK_TIMEOUT = 60 * 1000;
	AlarmDBHelper dbHelper = new AlarmDBHelper(this);
	AlarmObject mAlarmObject;
	TextView mTap;
	TextView mTimeRemaining;
	LinearLayout mTapLayout;

	int mTapCounter;
	int mTapCounterInitial;
	private final int mTapCounterMinimum = 30;
	private final int mTapCounterMaximum = 50;

	int mTimerCount = 24 * 1000;
	boolean mFinished = false;
	boolean mFailed = false;
	boolean mIsOnSnooze;
	Random r;

	Button mSnoozeButton;
	Button mDismissButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_alarm_screen_circle);

		final long id = getIntent().getLongExtra(AlarmConstants.ID, 0);
		String name = getIntent().getStringExtra(AlarmConstants.NAME);
		int timeHour = getIntent().getIntExtra(AlarmConstants.TIME_HOUR, 0);
		int timeMinute = getIntent().getIntExtra(AlarmConstants.TIME_MINUTE, 0);
		String tone = getIntent().getStringExtra(AlarmConstants.TONE);
		mIsOnSnooze = getIntent().getBooleanExtra(AlarmConstants.SNOOZE, false);

		TextView tvName = (TextView) findViewById(R.id.alarm_screen_name);
		tvName.setText(name);

		TextView tvTime = (TextView) findViewById(R.id.alarm_screen_time);
		tvTime.setText(String.format("%02d : %02d", timeHour, timeMinute));

		createMediaPlayer(tone);

		mTap = (TextView) findViewById(R.id.alarm_tap_number);
		mTimeRemaining = (TextView) findViewById(R.id.alarm_screen_time_remaining);

		listeners(id);

		Runnable releaseWakelock = createWakeLock();

		new Handler().postDelayed(releaseWakelock, WAKELOCK_TIMEOUT);
	}

	private Runnable createWakeLock() {
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
		return releaseWakelock;
	}

	private void listeners(final long id) {

		mDismissButton = (Button) findViewById(R.id.alarm_dismiss);
		mDismissButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				mPlayer.stop();
				finish();
			}
		});

		mSnoozeButton = (Button) findViewById(R.id.alarm_snooze);
		mSnoozeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {

				AlarmManagerHelper.cancelAlarms(AlarmScreenCircleActivity.this);

				AlarmObject object = dbHelper.getAlarm(id);
				if (object.timeMinute > 60 - getIntent().getIntExtra(
						AlarmConstants.COLUMN_NAME_ALARM_SNOOZE_TIME, 0)) {
					object.timeHour += 1;
					object.timeMinute = 60 % (object.timeMinute + getIntent()
							.getIntExtra(
									AlarmConstants.COLUMN_NAME_ALARM_SNOOZE_TIME,
									0));
				}

				object.timeMinute += getIntent().getIntExtra(
						AlarmConstants.COLUMN_NAME_ALARM_SNOOZE_TIME, 0);

				dbHelper.updateAlarm(object);

				AlarmManagerHelper.setAlarms(AlarmScreenCircleActivity.this);

				mPlayer.stop();
				finish();
			}
		});

		mTapLayout = (LinearLayout) findViewById(R.id.tap_layout);
		mTapLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mTapCounter >= 0 && !mFinished) {
					new ChangeUI().execute();
				}
			}
		});

	}

	private void createMediaPlayer(String tone) {
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
			mWakeLock = pm
					.newWakeLock(
							(PowerManager.FULL_WAKE_LOCK
									| PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP),
							TAG);
		}

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
		}

		r = new Random();
		mTapCounterInitial = mTapCounter = mTapCounterMinimum
				+ r.nextInt(mTapCounterMaximum - mTapCounterMinimum + 1);

		mTap.setText(String.valueOf(mTapCounter));

		new CountDownTimer(mTimerCount, 1) {

			public void onTick(long millisUntilFinished) {
				mTimeRemaining.setText(millisUntilFinished / 1000 + " "
						+ getResources().getString(R.string.timer));
				if (mTapCounter == 0) {
					if (mIsOnSnooze)
						mSnoozeButton.setVisibility(View.VISIBLE);
					mDismissButton.setVisibility(View.VISIBLE);
					mTimeRemaining.setText("Done!");
					mFinished = true;
					cancel();
				}
			}

			public void onFinish() {
				mFailed = true;
				new ChangeUI().execute();
				start();
			}

		}.start();

	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mWakeLock != null && mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	private class ChangeUI extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... imageView) {
			return null;
		}

		@Override
		protected void onPostExecute(Void view) {
			if (mFailed) {
				mTap.setText(String.valueOf(mTapCounterInitial));
				mTapCounter = mTapCounterInitial;
				mFailed = false;
			} else
				mTap.setText(String.valueOf(--mTapCounter));

			mTap.postInvalidate();
		}

	}

}
