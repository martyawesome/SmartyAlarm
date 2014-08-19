package com.martyawesome.smartyalarm.activities;

import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.martyawesome.smartyalarm.R;
import com.martyawesome.smartyalarm.database.AlarmDBHelper;

public class MainActivity extends ActionBarActivity {

	ImageView mAlarms;
	ImageView mSettings;
	ImageView mAbout;

	Bitmap mBitmap;

	float mScale;

	int mCounter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mScale = getResources().getDisplayMetrics().density;
		mAlarms = (ImageView) findViewById(R.id.alarms);
		mSettings = (ImageView) findViewById(R.id.settings);
		mAbout = (ImageView) findViewById(R.id.about);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		imageListeners();
	}

	private void imageListeners() {

		mAlarms.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new ChangeUI().execute(mAlarms);

				new Thread() {
					@Override
					public void run() {
						try {
							synchronized (this) {
								wait(200);
							}

							Intent intent = new Intent(MainActivity.this,
									AlarmsActivity.class);
							startActivity(intent);
						} catch (InterruptedException ex) {
						}
					}
				}.start();
			}
		});

		mSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				new ChangeUI().execute(mSettings);
				new Thread() {
					@Override
					public void run() {
						try {
							synchronized (this) {
								wait(200);
							}

							Intent intent = new Intent(MainActivity.this,
									AlarmScreenCircleActivity.class);
							startActivity(intent);
						} catch (InterruptedException ex) {
						}
					}
				}.start();
			}
		});

		mAbout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new ChangeUI().execute(mAbout);
				new Thread() {
					@Override
					public void run() {
						try {
							synchronized (this) {
								wait(200);
							}

							Intent intent = new Intent(MainActivity.this,
									AlarmScreenMathActivity.class);
							startActivity(intent);
						} catch (InterruptedException ex) {
						}
					}
				}.start();
			}
		});
	}

	private class ChangeUI extends AsyncTask<ImageView, ImageView, ImageView> {

		@Override
		protected ImageView doInBackground(ImageView... imageView) {

			return imageView[0];
		}

		@Override
		protected void onPostExecute(final ImageView imageView) {
			super.onPostExecute(imageView);

			new Thread() {
				public void run() {
					while (mCounter++ < 5) {
						try {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									final int padding = (int) (1 * mScale + 0.5f);
									if (mCounter < 3) {
										imageView.setPadding(
												imageView.getPaddingLeft()
														- padding,
												imageView.getPaddingTop()
														- padding,
												imageView.getPaddingRight()
														- padding,
												imageView.getPaddingBottom()
														- padding);
									} else {
										imageView.setPadding(
												imageView.getPaddingLeft()
														+ padding,
												imageView.getPaddingTop()
														+ padding,
												imageView.getPaddingRight()
														+ padding,
												imageView.getPaddingBottom()
														+ padding);
									}
									imageView.postInvalidate();
								}
							});
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					mCounter = 0;
				}
			}.start();
		}

	}
	


}
