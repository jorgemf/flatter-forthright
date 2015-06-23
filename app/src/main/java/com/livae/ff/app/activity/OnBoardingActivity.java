package com.livae.ff.app.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.R;
import com.livae.ff.app.fragment.OnBoardingPaymentFragment;
import com.livae.ff.app.fragment.OnBoardingVerifyNumberFragment;
import com.livae.ff.app.fragment.OnBoardingWarningFragment;
import com.livae.ff.app.fragment.OnBoardingWelcomeFragment;
import com.livae.ff.app.service.ContactsService;

public class OnBoardingActivity extends AbstractActivity {

	private ON_BOARDING currentStep;

	public static void start(Activity activity) {
		Intent intent = new Intent(activity, OnBoardingActivity.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity)
														  .toBundle());
		} else {
			activity.startActivity(intent);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Analytics.screen(Analytics.Screen.ON_BOARDING);
		// update contacts database
		startService(new Intent(this, ContactsService.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_right,
																		   R.anim.slide_out_left)
								   .replace(R.id.container, fragment).commit();
	}

	enum ON_BOARDING {WELCOME, PHONE, WARNING, PAYMENT}
}
