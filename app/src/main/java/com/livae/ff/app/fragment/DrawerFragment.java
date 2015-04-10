package com.livae.ff.app.fragment;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.activity.AboutActivity;
import com.livae.ff.app.activity.AppHuntActivity;
import com.livae.ff.app.activity.SettingsActivity;
import com.livae.ff.app.admin.activity.AdminActivity;
import com.livae.ff.app.listener.LoginListener;
import com.livae.ff.app.utils.LoginUtils;

public class DrawerFragment extends Fragment implements View.OnClickListener, LoginListener {

	private Button signInGoogleButton;

	private Button[] drawerButtons;

	private Drawable[] drawablesBackgrounds;

	private View adminButton;

//	private ImageView flagImage;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_drawer, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		drawerButtons = new Button[SelectedFragment.values().length];
		int frontPagePos = SelectedFragment.FRONT_PAGE.ordinal();
		drawerButtons[frontPagePos] = (Button) view.findViewById(R.id.button_front_page);
		int browseByDatePos = SelectedFragment.BROWSE_BY_DATE.ordinal();
		drawerButtons[browseByDatePos] = (Button) view.findViewById(R.id.button_browse_by_date);
		int votedPos = SelectedFragment.VOTED.ordinal();
		drawerButtons[votedPos] = (Button) view.findViewById(R.id.button_my_voted_apps);
		int sharedPos = SelectedFragment.SHARED.ordinal();
		drawerButtons[sharedPos] = (Button) view.findViewById(R.id.button_my_shared_apps);
		int commentedPos = SelectedFragment.COMMENTED.ordinal();
		drawerButtons[commentedPos] = (Button) view.findViewById(R.id.button_my_commented_apps);
		int i = 0;
		drawablesBackgrounds = new Drawable[drawerButtons.length];
		for (Button button : drawerButtons) {
			button.setOnClickListener(this);
			drawablesBackgrounds[i] = button.getBackground();
			i++;
		}

		view.findViewById(R.id.button_settings).setOnClickListener(this);
		view.findViewById(R.id.button_about).setOnClickListener(this);
		adminButton = view.findViewById(R.id.button_admin);
		adminButton.setOnClickListener(this);

//		flagImage = (ImageView) view.findViewById(R.id.flag_icon);

		signInGoogleButton = (Button) view.findViewById(R.id.button_sign_in_google);
		signInGoogleButton.setOnClickListener(this);
		reviewLogin();
	}

	@Override
	public void onResume() {
		super.onResume();
		reviewLogin();
		if (Application.getSeeAdmin()) {
			adminButton.setVisibility(View.VISIBLE);
		} else {
			adminButton.setVisibility(View.GONE);
		}
//		Constants.COUNTRY country = Application.appUser().getCountry();
//		if (country != null) {
//			flagImage.setImageResource(country.getCountryFlagResId());
//		}
	}

	public void selectedButton(SelectedFragment selectedFragment) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			for (int i = 0; i < drawerButtons.length; i++) {
				drawerButtons[i].setBackground(drawablesBackgrounds[i]);
			}
		} else {
			for (int i = 0; i < drawerButtons.length; i++) {
				drawerButtons[i].setBackgroundResource(android.R.drawable.menuitem_background);
			}
		}
		int color = getResources().getColor(R.color.darker_blue);
		int pos = selectedFragment.ordinal();
		drawerButtons[pos].setBackgroundColor(color);
	}

	@Override
	public void onClick(View v) {
		AppHuntActivity appHuntActivity = (AppHuntActivity) getActivity();
		switch (v.getId()) {
			case R.id.button_sign_in_google:
				LoginUtils.showLoginGoogleFragment(getActivity(), false, this);
				break;
			case R.id.button_front_page:
				appHuntActivity.selectedFragment(SelectedFragment.FRONT_PAGE);
				break;
			case R.id.button_browse_by_date:
				appHuntActivity.selectedFragment(SelectedFragment.BROWSE_BY_DATE);
				break;
			case R.id.button_my_voted_apps:
				appHuntActivity.selectedFragment(SelectedFragment.VOTED);
				break;
			case R.id.button_my_shared_apps:
				appHuntActivity.selectedFragment(SelectedFragment.SHARED);
				break;
			case R.id.button_my_commented_apps:
				appHuntActivity.selectedFragment(SelectedFragment.COMMENTED);
				break;
			case R.id.button_settings:
				SettingsActivity.start(getActivity());
				break;
			case R.id.button_about:
				AboutActivity.start(getActivity());
				break;
			case R.id.button_admin:
				if (Application.isAdmin()) {
					AdminActivity.start(getActivity());
				}
				break;
		}
	}

	public void reviewLogin() {
		if (isResumed()) {
			if (!Application.appUser().isAnonymous()) {
				signInGoogleButton.setVisibility(View.GONE);
			} else {
				signInGoogleButton.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void login() {
		signInGoogleButton.setVisibility(View.GONE);
	}

	@Override
	public void logout() {
		signInGoogleButton.setVisibility(View.VISIBLE);
	}

	public enum SelectedFragment {
		FRONT_PAGE, BROWSE_BY_DATE, VOTED, SHARED,
		COMMENTED
	}
}
