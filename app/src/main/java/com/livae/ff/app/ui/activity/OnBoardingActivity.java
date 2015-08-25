package com.livae.ff.app.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.R;
import com.livae.ff.app.sql.DBHelper;
import com.livae.ff.app.ui.fragment.OnBoardingPaymentFragment;
import com.livae.ff.app.ui.fragment.OnBoardingVerifyNumberFragment;
import com.livae.ff.app.ui.fragment.OnBoardingWarningFragment;
import com.livae.ff.app.ui.fragment.OnBoardingWelcomeFragment;

public class OnBoardingActivity extends AbstractActivity {

	private ON_BOARDING currentStep;

	public static void start(AbstractActivity activity) {
		startActivity(activity, OnBoardingActivity.class, null, null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Analytics.screen(Analytics.Screen.ON_BOARDING);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Handler handler = new Handler();
		handler.post(new Runnable() {
			@Override
			public void run() {
				DBHelper.clearData(OnBoardingActivity.this);
			}
		});
		setContentView(R.layout.activity_on_boarding);
		if (savedInstanceState == null) {
			final FragmentManager fragmentManager = getSupportFragmentManager();
			final OnBoardingWelcomeFragment fragment = new OnBoardingWelcomeFragment();
			fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
			currentStep = ON_BOARDING.WELCOME;
		}
	}

	public void nextStep() {
		if (currentStep == null) {
			setFragment(new OnBoardingWelcomeFragment());
			currentStep = ON_BOARDING.WELCOME;
		} else {
			switch (currentStep) {
				case WELCOME:
					setFragment(new OnBoardingVerifyNumberFragment());
					currentStep = ON_BOARDING.PHONE;
					break;
				case PHONE:
					setFragment(new OnBoardingWarningFragment());
					currentStep = ON_BOARDING.WARNING;
					break;
				case WARNING:
					setFragment(new OnBoardingPaymentFragment());
					currentStep = ON_BOARDING.PAYMENT;
					break;
				case PAYMENT:
					ChatsActivity.start(this);
					finish();
					break;
			}
		}
	}

	private void setFragment(Fragment fragment) {
		getSupportFragmentManager().beginTransaction()
								   .setCustomAnimations(R.anim.slide_in_right,
														R.anim.slide_out_left)
								   .replace(R.id.container, fragment)
								   .commit();
	}

	enum ON_BOARDING {WELCOME, PHONE, WARNING, PAYMENT}
}
