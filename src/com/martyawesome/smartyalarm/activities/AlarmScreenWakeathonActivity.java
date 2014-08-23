package com.martyawesome.smartyalarm.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.martyawesome.smartyalarm.AlarmConstants;
import com.martyawesome.smartyalarm.AlarmObject;
import com.martyawesome.smartyalarm.R;
import com.martyawesome.smartyalarm.database.AlarmDBHelper;
import com.martyawesome.smartyalarm.services.AlarmManagerHelper;
import com.martyawesome.smartyalarm.services.ShakeDetector;
import com.martyawesome.smartyalarm.services.ShakeDetector.OnShakeListener;

public class AlarmScreenWakeathonActivity extends Activity implements
		LocationListener {

	public final String TAG = this.getClass().getSimpleName();
	private WakeLock mWakeLock;
	private MediaPlayer mPlayer;
	AlarmDBHelper dbHelper = new AlarmDBHelper(this);
	AlarmObject mAlarmObject;
	Location location; // location
	protected LocationManager locationManager;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ShakeDetector mShakeDetector;
	boolean isGPSEnabled = false; // flag for GPS status
	boolean isNetworkEnabled = false; // flag for network status
	boolean isPassiveEnabled = false;
	boolean finishedWalking = false;
	boolean mIsOnSnooze;
	double startingLongitude;
	double startingLatitude;
	double latitude; // latitude
	double longitude; // longitude
	int mMeterCovered = 0;
	int mShakeCount = 0;
	int mTimeHour;
	int mTimeMinute;
	String mName;
	TextView mWakeathonTime;
	TextView mWakeathonMessage;
	TextView mTvName;
	TextView mTvTime;
	TextView mStart;
	Button mDismissButton;
	Button mSnoozeButton;
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meter
	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 1; // 1 second
	private static final int WAKELOCK_TIMEOUT = 60 * 1000;
	Typeface tf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_screen);

		findViewById(R.id.buttons).setVisibility(View.GONE);
		findViewById(R.id.word).setVisibility(View.GONE);
		findViewById(R.id.tap).setVisibility(View.GONE);
		findViewById(R.id.math).setVisibility(View.GONE);
		findViewById(R.id.wakeathon).setVisibility(View.VISIBLE);

		final long id = getIntent().getLongExtra(AlarmConstants.ID, 0);
		mName = getIntent().getStringExtra(AlarmConstants.NAME);
		mTimeHour = getIntent().getIntExtra(AlarmConstants.TIME_HOUR, 0);
		mTimeMinute = getIntent().getIntExtra(AlarmConstants.TIME_MINUTE, 0);
		String tone = getIntent().getStringExtra(AlarmConstants.TONE);
		mIsOnSnooze = getIntent().getBooleanExtra(AlarmConstants.SNOOZE, false);

		mWakeathonTime = (TextView) findViewById(R.id.alarm_screen_time_wakeathon_distance_remaining);
		mWakeathonMessage = (TextView) findViewById(R.id.alarm_screen_message);
		mTvName = (TextView) findViewById(R.id.alarm_screen_name_wakeathon);
		mTvTime = (TextView) findViewById(R.id.alarm_screen_time_wakeathon);
		mStart = (TextView) findViewById(R.id.alarm_screen_start_wakeathon);

		int actionBarTitle = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");
		TextView actionBarTitleView = (TextView) getWindow().findViewById(
				actionBarTitle);
		tf = Typeface.createFromAsset(getAssets(),
				AlarmConstants.APP_FONT_STYLE);
		actionBarTitleView.setTypeface(tf);
		mWakeathonTime.setTypeface(tf);
		mStart.setTypeface(tf);

		mTvName.setText(mName);
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

		// getLocation();

		createMediaPlayer(tone);
		listeners(id);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mShakeDetector = new ShakeDetector(new OnShakeListener() {

			@Override
			public void onShake() {
				if (finishedWalking) {
					++mShakeCount;
					if (mShakeCount == 5) {
						initializeWakeUpButtons();
						mShakeCount = 0;
					}
				}
			}
		});

		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		mStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				isGPSEnabled = locationManager
						.isProviderEnabled(LocationManager.GPS_PROVIDER);
				isNetworkEnabled = locationManager
						.isProviderEnabled(LocationManager.NETWORK_PROVIDER);	
				isPassiveEnabled = locationManager
						.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

				if (!isGPSEnabled && !isNetworkEnabled && !isPassiveEnabled)
					showSettingsAlert();
				else {
					getLocation();
					mWakeathonTime.setVisibility(View.VISIBLE);
					mWakeathonMessage.setVisibility(View.VISIBLE);
					mStart.setVisibility(View.GONE);
				}
			}
		});

		Runnable releaseWakelock = createWakeLock();
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
			mWakeLock = pm
					.newWakeLock(
							(PowerManager.FULL_WAKE_LOCK
									| PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP),
							TAG);
		}

		if (!mWakeLock.isHeld()) {
			mWakeLock.acquire();
		}

		mSensorManager.registerListener(mShakeDetector, mAccelerometer,
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(mShakeDetector);
	}

	public void getLocation() {
		try {
			if (isNetworkEnabled) {
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
						MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

				if (locationManager != null) {
					location = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null) {
						latitude = location.getLatitude();
						longitude = location.getLongitude();
					}
				}
			}
			if (isPassiveEnabled) {
				if (location == null) {
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
			}
			// if GPS Enabled get lat/long using GPS Services
			if (isGPSEnabled) {
				if (location == null) {
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(AlarmScreenWakeathonActivity.this,
				"Provider enabled: " + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(AlarmScreenWakeathonActivity.this,
				"Provider disabled: " + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();

		new ChangeUI().execute();
	}

	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}

	public void turnWIFIOn() {
		WifiManager wifiManager;
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true); // True - to enable WIFI connectivity
											// .
	}

	public void turnWIFIOff() {
		WifiManager wifiManager;
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiManager.setWifiEnabled(false); // True - to enable WIFI connectivity
											// .
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

				AlarmManagerHelper
						.cancelAlarms(AlarmScreenWakeathonActivity.this);

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

				AlarmManagerHelper.setAlarms(AlarmScreenWakeathonActivity.this);

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

	/**
	 * Function to show settings alert dialog On pressing Settings button will
	 * lauch Settings Options
	 * */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog
				.setMessage("Internet or GPS is not enabled. Alarm will never be turned off it your Internet or GPS is disabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);
					}
				});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		// Showing Alert Message
		alertDialog.show();
	}

	private void initializeWakeUpButtons() {
		TextView tvName = (TextView) findViewById(R.id.alarm_screen_buttons_name);
		TextView tvTime = (TextView) findViewById(R.id.alarm_screen_buttons_time);

		findViewById(R.id.buttons).setVisibility(View.VISIBLE);
		findViewById(R.id.math).setVisibility(View.GONE);
		if (mIsOnSnooze)
			mSnoozeButton.setVisibility(View.VISIBLE);

		tvName.setText(mName);
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
	}

	private class ChangeUI extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... imageView) {
			return null;
		}

		@Override
		protected void onPostExecute(Void view) {			
			if (mMeterCovered == 1) {
				mWakeathonMessage.setText(getResources().getString(
						R.string.wake_up));
				mWakeathonTime.setText(String.valueOf(3 - mMeterCovered)
						+ " meters remaining");
			} else if (mMeterCovered == 2) {
				mWakeathonMessage.setText(getResources().getString(
						R.string.keep_walking));
				mWakeathonTime.setText(String.valueOf(3 - mMeterCovered)
						+ " meter remaining");
			}
			
			mMeterCovered++;

			if (mMeterCovered == 3) {
				stopUsingGPS();
				mMeterCovered = 0;
				finishedWalking = true;
				ImageView rotate = (ImageView) findViewById(R.id.alarm_screen_shake_imageView);
				rotate.setVisibility(View.VISIBLE);
				mWakeathonMessage
						.setVisibility(View.GONE);
				mWakeathonTime.setVisibility(View.GONE);
			}

		}
	}

}
