package com.livae.ff.app.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.livae.ff.app.Analytics;
import com.livae.ff.app.R;

public class ContactsActivity extends AbstractActivity {

	public static final String SELECTED_PHONE = "SELECTED_PHONE";

	public static final String SELECTED_DISPLAY_NAME = "SELECTED_DISPLAY_NAME";

	public static final String SELECTED_USER_BLOCKED = "SELECTED_USER_BLOCKED";

	public static final String SELECTED_RAW_CONTACT_ID = "SELECTED_RAW_CONTACT_ID";

	public static void start(AbstractActivity activity) {
		startActivity(activity, ContactsActivity.class, null, null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Analytics.screen(Analytics.Screen.CONTACTS);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
}
