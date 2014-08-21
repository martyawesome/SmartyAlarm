package com.martyawesome.smartyalarm.activities;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
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

public class AlarmScreenWordActivity extends Activity {

	public final String TAG = this.getClass().getSimpleName();

	private WakeLock mWakeLock;
	private MediaPlayer mPlayer;

	private static final int WAKELOCK_TIMEOUT = 60 * 1000;
	AlarmDBHelper dbHelper = new AlarmDBHelper(this);
	AlarmObject mAlarmObject;
	int mCorrectRemaining = 3;
	int mWordLengthMinimum = 5;
	int mWordLengthMaximum = 10;
	boolean mIsOnSnooze;
	boolean mFinished = false;
	Random r;
	Button mSnoozeButton;
	Button mDismissButton;
	Button mSubmit;
	TextView mWord;
	TextView mSolveRemaining;
	TextView mTvName;
	TextView mTvTime;
	EditText mGetAnswer;
	String[] mAlphabet;
	String name;
	String mAnswer = "";
	int mTimeHour;
	int mTimeMinute;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_screen);

		findViewById(R.id.buttons).setVisibility(View.GONE);
		findViewById(R.id.word).setVisibility(View.VISIBLE);
		findViewById(R.id.tap).setVisibility(View.GONE);
		findViewById(R.id.math).setVisibility(View.GONE);

		final long id = getIntent().getLongExtra(AlarmConstants.ID, 0);
		name = getIntent().getStringExtra(AlarmConstants.NAME);
		mTimeHour = getIntent().getIntExtra(AlarmConstants.TIME_HOUR, 0);
		mTimeMinute = getIntent().getIntExtra(AlarmConstants.TIME_MINUTE, 0);
		String tone = getIntent().getStringExtra(AlarmConstants.TONE);
		mIsOnSnooze = getIntent().getBooleanExtra(AlarmConstants.SNOOZE, false);

		mTvName = (TextView) findViewById(R.id.alarm_screen_name_word);
		mTvTime = (TextView) findViewById(R.id.alarm_screen_time_word);
		mSolveRemaining = (TextView) findViewById(R.id.word_correct);
		mWord = (TextView) findViewById(R.id.word_textView);
		mSubmit = (Button) findViewById(R.id.submit);
		mGetAnswer = (EditText) findViewById(R.id.answer);

		mAlphabet = getResources().getStringArray(R.array.alphabet);

		mTvName.setText(name);
		mTvTime.setText(String.format("%02d : %02d", mTimeHour, mTimeMinute));

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
		mSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mGetAnswer.getText().toString().length() > 0) {
					if (mGetAnswer.getText().toString().equals(mAnswer)) {
						new ChangeUI().execute();
						mAnswer = "";
						initializeWord();

					} else {
						Toast.makeText(AlarmScreenWordActivity.this,
								"Incorrect Answer", Toast.LENGTH_SHORT).show();
					}
				} else
					Toast.makeText(AlarmScreenWordActivity.this,
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

				AlarmManagerHelper.cancelAlarms(AlarmScreenWordActivity.this);

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

				AlarmManagerHelper.setAlarms(AlarmScreenWordActivity.this);

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

		initializeWord();

	}

	private void initializeWord() {
		r = new Random();
		int wordLength = mWordLengthMinimum
				+ r.nextInt(mWordLengthMaximum - mWordLengthMinimum + 1);

		for (int i = 0; i < wordLength; i++) {
			if (r.nextInt(2) == 0)
				mAnswer += mAlphabet[r.nextInt(mAlphabet.length)].toUpperCase();
			else
				mAnswer += mAlphabet[r.nextInt(mAlphabet.length)].toLowerCase();
		}

		mWord.setText(mAnswer);
		mSolveRemaining.setText(String.valueOf(mCorrectRemaining)
				+ " Correct Answers Remaining");
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
			tvTime.setText(String.format("%02d : %02d", mTimeHour, mTimeMinute));
		}

	}

}