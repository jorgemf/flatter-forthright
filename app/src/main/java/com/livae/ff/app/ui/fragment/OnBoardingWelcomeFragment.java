package com.livae.ff.app.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.ui.activity.OnBoardingActivity;

public class OnBoardingWelcomeFragment extends AbstractFragment implements View.OnClickListener {

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_on_boarding_welcome, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.button).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button:
				OnBoardingActivity onBoardingActivity = (OnBoardingActivity) getActivity();
				onBoardingActivity.nextStep();
				break;
		}
	}
}
