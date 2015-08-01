package com.livae.ff.app.activity;

import android.accounts.NetworkErrorException;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.livae.ff.app.R;
import com.livae.ff.app.listener.OnLifeCycleListener;
import com.livae.ff.app.utils.SyncUtils;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public abstract class AbstractActivity extends AppCompatActivity {

	protected static final String LOG_TAG = "ACTIVITY";

	private Toolbar toolbar;

	private List<OnLifeCycleListener> onLifeCycleListenerList;

	private String getExceptionError(@Nonnull Exception e) {
		if (e instanceof NetworkErrorException) {
			return getString(R.string.error_network_error);
		} else if (e instanceof ConnectException) {
			return getString(R.string.error_network_no_api_connection);
		} else if (e instanceof GoogleJsonResponseException) {
			Log.e(LOG_TAG, e.getMessage(), e);
			GoogleJsonResponseException ge = (GoogleJsonResponseException) e;
			if (ge.getDetails() != null) {
				return getString(R.string.error_unexpected, ge.getDetails().getMessage());
			} else {
				return getString(R.string.error_unknown, e.getMessage());
			}
		} else {
			Log.e(LOG_TAG, e.getMessage(), e);
			return getString(R.string.error_unknown, e.getMessage());
		}
	}

	@Override
	protected void onRestoreInstanceState(@Nonnull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				break;
			default:
				return false;
		}
		return true;
	}

	public Toolbar getToolbar() {
		return toolbar;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			finishAfterTransition();
		}
	}

	@Override
	protected void onPause() {
		for (OnLifeCycleListener listener : onLifeCycleListenerList) {
			listener.onPause();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!(this instanceof OnBoardingActivity) && !SyncUtils.isAccountRegistered(this)) {
			OnBoardingActivity.start(this);
			finish();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public void showSnackBarException(Exception e) {
		Snackbar.make(findViewById(R.id.container), getExceptionError(e), Snackbar.LENGTH_LONG)
				.show();
	}

	public void showSnackBarException(String message) {
		Snackbar.make(findViewById(R.id.container), message, Snackbar.LENGTH_LONG).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onLifeCycleListenerList = new ArrayList<>();
	}

	public void setSupportActionBar(Toolbar toolbar) {
		this.toolbar = toolbar;
		super.setSupportActionBar(toolbar);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		for (OnLifeCycleListener listener : onLifeCycleListenerList) {
			listener.onConfigurationChanges();
		}
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onDestroy() {
		for (OnLifeCycleListener listener : onLifeCycleListenerList) {
			listener.onDestroy();
		}
		super.onDestroy();
	}

	public void addLifeCycleListener(OnLifeCycleListener cycleListener) {
		if (!onLifeCycleListenerList.contains(cycleListener)) {
			onLifeCycleListenerList.add(cycleListener);
		}
	}

	public void removeLifeCycleListener(OnLifeCycleListener cycleListener) {
		onLifeCycleListenerList.remove(cycleListener);
	}
}
