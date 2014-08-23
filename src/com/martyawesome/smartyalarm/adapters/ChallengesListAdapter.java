package com.martyawesome.smartyalarm.adapters;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.martyawesome.smartyalarm.AlarmConstants;
import com.martyawesome.smartyalarm.ChallengeObject;
import com.martyawesome.smartyalarm.R;
import com.martyawesome.smartyalarm.activities.SettingsActivity;
import com.martyawesome.smartyalarm.database.AlarmDBHelper;

public class ChallengesListAdapter extends BaseAdapter {

	private Context mContext;
	private List<ChallengeObject> mChallenges;
	private AlarmDBHelper dbHelper;
	public ChallengeObject mObject;
	private Typeface tf;

	public ChallengesListAdapter(Context context,
			List<ChallengeObject> challenges) {
		mContext = context;

		dbHelper = new AlarmDBHelper(mContext);
		mChallenges = challenges;

		int actionBarTitle = Resources.getSystem().getIdentifier(
				"action_bar_title", "id", "android");
		TextView actionBarTitleView = (TextView) ((Activity) context)
				.getWindow().findViewById(actionBarTitle);
		tf = Typeface.createFromAsset(context.getAssets(),
				AlarmConstants.APP_FONT_STYLE);
		actionBarTitleView.setTypeface(tf);
	}

	public void setchallenges(List<ChallengeObject> challenges) {
		mChallenges = challenges;
	}

	@Override
	public int getCount() {
		if (mChallenges != null) {
			return mChallenges.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mChallenges != null) {
			return mChallenges.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {

		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater
					.inflate(R.layout.challenge_list_item, parent, false);
		}

		mObject = (ChallengeObject) getItem(position);

		TextView txtName = (TextView) view
				.findViewById(R.id.challenge_item_name);
		txtName.setText(mObject.name);
		txtName.setTypeface(tf);

		final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox1);
		checkBox.setChecked(mObject.isEnabled);
		checkBox.setTag(mObject.id);

		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				if (!isChecked) {
					if (dbHelper.getEnabledChallenges().size() > 1) {
						((SettingsActivity) mContext).updateChallenge(
								((int) buttonView.getTag()), isChecked);
					} else {
						Toast.makeText(
								mContext,
								mContext.getResources().getString(
										R.string.challengeEnabledOne),
								Toast.LENGTH_LONG).show();

						buttonView.setChecked(true);
					}
				} else
					((SettingsActivity) mContext).updateChallenge(
							((int) buttonView.getTag()), isChecked);
			}
		});

		return view;

	}
}
