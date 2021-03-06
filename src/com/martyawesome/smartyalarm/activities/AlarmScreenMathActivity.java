package com.martyawesome.smartyalarm.activities;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.martyawesome.smartyalarm.AlarmConstants;
import com.martyawesome.smartyalarm.AlarmObject;
import com.martyawesome.smartyalarm.R;
import com.martyawesome.smartyalarm.database.AlarmDBHelper;
import com.martyawesome.smartyalarm.services.AlarmManagerHelper;

public class AlarmScreenMathActivity extends Activity {

	public final String TAG = this.getClass().getSimpleName();

	private WakeLock mWakeLock;
	private MediaPlayer mPlayer;

	private static final int WAKELOCK_TIMEOUT = 60 * 1000;
	AlarmDBHelper dbHelper = new AlarmDBHelper(this);
	AlarmObject mAlarmObject;
	int mCorrectRemaining = 3;
	int mMathValue1;
	int mMathValue2;
	int mOutput;
	int mMathValueMinimum = 20;
	int mMathValueMaximum = 40;
	boolean mIsOnSnooze;
	boolean mFinished = false;
	Random r;
	Button mSnoozeButton;
	Button mDismissButton;
	Button mMathSubmit;
	TextView mValue1;
	TextView mValue2;
	TextView mOperator;
	TextView mSolveRemaining;
	TextView mTvName;
	TextView mTvTime;
	EditText mGetAnswer;
	String[] mMathOperators;
	String name;
	int mTimeHour;
	int mTimeMinute;
	Typeface tf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_screen);

		findViewById(R.id.buttons).setVisibility(View.GONE);
		findViewById(R.id.word).setVisibility(View.GONE);
		findViewById(R.id.tap).setVisibility(View.GONE);
		findViewById(R.id.math).setVisibility(View.VISIBLE);
		findViewById(R.id.wakeathon).setVisibility(View.GONE);

		final long id = getIntent().getLongExtra(AlarmConstants.ID, 0);
		name = getIntent().getStringExtra(AlarmConstants.NAME);
		mTimeHour = getIntent().getIntExtra(AlarmConstants.TIME_HOUR, 0);
		mTimeMinute = getIntent().getIntExtra(AlarmConstants.TIME_MINUTE, 0);
		String tone = getIntent().getStringExtra(AlarmConstants.TONE);
		mIsOnSnooze = getIntent().getBooleanExtra(AlarmConstants.SNOOZE, false);

		mTvName = (TextView) findViewById(R.id.alarm_screen_name_math);
		mTvTime = (TextView) findViewById(R.id.alarm_screen_time_math);
		mSolveRemaining = (TextView) findViewById(R.id.math_correct);
		mValue1 = (TextView) findViewById(R.id.math_value_1);
		mValue2 = (TextView) findViewById(R.id.math_value_2);
		mOperator = (TextView) findViewById(R.id.math_operator);
		mMathSubmit = (Button) findViewById(R.id.math_submit);
		mGetAnswer = (EditText) findViewById(R.id.sum);

		int actionBarTitle = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");
		TextView actionBarTitleView = (TextView) getWindow().findViewById(
				actionBarTitle);
		tf = Typeface.createFromAsset(getAssets(),
				AlarmConstants.APP_FONT_STYLE);
		actionBarTitleView.setTypeface(tf);
		mTvName.setTypeface(tf);
		mTvTime.setTypeface(tf);
		mSolveRemaining.setTypeface(tf);
		mGetAnswer.setTypeface(tf);
		mValue1.setTypeface(tf);
		mValue2.setTypeface(tf);
		mMathSubmit.setTypeface(tf);
		mOperator.setTypeface(tf);

		mMathOperators = getResources().getStringArray(R.array.math_operators);

		mTvName.setText(name);
		if (mTimeHour > 12) {
			mTvTime.setText(String.format("%02d : %02d pm", mTimeHour - 12,
					mTimeMinute));
		} else if (mTimeHour < 12) {
			mTvTime.setText(String.format("%02d : %02d am", mTimeHour,
					mTimeMinute));
		} else {
			mTvTime.setText(String.format("%02d : %02d pm", mTimeHour,
					mTimeMinute));
		}

		createMediaPlayer(tone);
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
		mMathSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mGetAnswer.getText().toString().length() > 0) {
					if (mOutput == Integer.parseInt(mGetAnswer.getText()
							.toString())) {

						new ChangeUI().execute();
						initializeNumbers();

					} else {
						initializeNumbers();
						Toast.makeText(AlarmScreenMathActivity.this,
								"Incorrect Answer", Toast.LENGTH_SHORT).show();
					}
				} else
					Toast.makeText(AlarmScreenMathActivity.this,
							getResources().getString(R.string.empty_edit_text),
							Toast.LENGTH_SHORT).show();

				mGetAnswer.setText("");
			}
		});

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

				AlarmManagerHelper.cancelAlarms(AlarmScreenMathActivity.this);

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

				AlarmManagerHelper.setAlarms(AlarmScreenMathActivity.this);

				mPlayer.stop();
				finish();
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

		initializeNumbers();

	}

	private void initializeNumbers() {
		r = new Random();
		mMathValue1 = mMathValueMinimum
				+ r.nextInt(mMathValueMaximum - mMathValueMinimum + 1);
		mMathValue2 = mMathValueMinimum
				+ r.nextInt(mMathValueMaximum - mMathValueMinimum + 1);

		int randomOperator = r.nextInt(mMathOperators.length);

		if (randomOperator == 1) {
			if (mMathValue2 > mMathValue1) {
				int temp = mMathValue2;
				mMathValue2 = mMathValue1;
				mMathValue1 = temp;
			}
		}

		mOperator.setText(mMathOperators[randomOperator]);

		switch (randomOperator) {
		case 0:
			mOutput = mMathValue1 + mMathValue2;
			break;
		case 1:
			mOutput = mMathValue1 - mMathValue2;
			break;
		case 2:
			mMathValue1 = 2 + r.nextInt(9);
			mMathValue2 = 2 + r.nextInt(9);
			mOutput = mMathValue1 * mMathValue2;

			break;
		default:
			mOutput = mMathValue1 + mMathValue2;
		}

		mSolveRemaining.setText(String.valueOf(mCorrectRemaining)
				+ " Correct Answers Remaining");
		mValue1.setText(String.valueOf(mMathValue1));
		mValue2.setText(String.valueOf(mMathValue2));
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
			mSolveRemaining.setText(String.valueOf(--mCorrectRemaining)
					+ " Correct Answers Remaining");

			if (mCorrectRemaining == 0) {
				findViewById(R.id.buttons).setVisibility(View.VISIBLE);
				findViewById(R.id.math).setVisibility(View.GONE);
				if (mIsOnSnooze)
					mSnoozeButton.setVisibility(View.VISIBLE);
				initializeWakeUpButtons();
			}
		}

		private void initializeWakeUpButtons() {
			TextView tvName = (TextView) findViewById(R.id.alarm_screen_buttons_name);
			TextView tvTime = (TextView) findViewById(R.id.alarm_screen_buttons_time);
			tvName.setText(name);
			if (mTimeHour > 12) {
				tvTime.setText(String.format("%02d : %02d pm", mTimeHour - 12,
						mTimeMinute));
			} else if (mTimeHour < 12) {
				tvTime.setText(String.format("%02d : %02d am", mTimeHour - 12,
						mTimeMinute));
			} else {
				tvTime.setText(String.format("%02d : %02d pm", mTimeHour - 12,
						mTimeMinute));
			}
			mDismissButton.setTypeface(tf);
			mSnoozeButton.setTypeface(tf);
			tvName.setTypeface(tf);
			tvTime.setTypeface(tf);
		}

	}

}