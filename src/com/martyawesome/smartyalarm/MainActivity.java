package com.martyawesome.smartyalarm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

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

		imageListeners();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void imageListeners() {
		mAlarms.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				imageViewOnClickAnimation(mAlarms);
				Intent intent = new Intent(MainActivity.this,
						AlarmsActivity.class);
				startActivity(intent);
			}
		});

		mSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				imageViewOnClickAnimation(mSettings);
			}
		});

		mAbout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				imageViewOnClickAnimation(mAbout);
			}
		});
	}

	private void imageViewOnClickAnimation(final ImageView imageView) {
		new Thread() {
			public void run() {
				while (mCounter++ < 10) {
					try {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								final int padding = (int) (1 * mScale + 0.5f);
								if (mCounter < 5) {
									imageView.setPadding(
											imageView.getPaddingLeft() - padding,
											imageView.getPaddingTop() - padding,
											imageView.getPaddingRight() - padding,
											imageView.getPaddingBottom()
													- padding);
								} else {
									imageView.setPadding(
											imageView.getPaddingLeft() + padding,
											imageView.getPaddingTop() + padding,
											imageView.getPaddingRight() + padding,
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
