package com.livae.ff.app.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.adapter.ChatsFragmentsAdapter;
import com.livae.ff.app.service.ContactsService;

public class ChatsActivity extends AbstractActivity {

	public static void start(Activity activity) {
		Intent intent = new Intent(activity, ChatsActivity.class);
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
		Analytics.screen(Analytics.Screen.CHATS);
		// update contacts database
		startService(new Intent(this, ContactsService.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chats_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case R.id.menu_feedback:
				// TODO
				break;
			case R.id.menu_settings:
				// TODO
				break;
			case R.id.menu_share:
				// TODO
				break;
			default:
				return super.onOptionsItemSelected(menuItem);
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Application.appUser().getUserPhone() == null) {
			OnBoardingActivity.start(this);
			finish();
		} else {
			setContentView(R.layout.activity_chats);

			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);
			TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
//			tabLayout.addTab(tabLayout.newTab().setCustomView());
			ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
			viewPager.setAdapter(new ChatsFragmentsAdapter(getSupportFragmentManager(),
														   getResources()));
			tabLayout.setupWithViewPager(viewPager);
		}
	}
}
