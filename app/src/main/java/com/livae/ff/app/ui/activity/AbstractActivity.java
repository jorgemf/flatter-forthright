package com.livae.ff.app.ui.activity;

import android.accounts.NetworkErrorException;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.async.AsyncCache;
import com.livae.ff.app.listener.LifeCycle;
import com.livae.ff.app.listener.OnLifeCycleListener;
import com.livae.ff.app.utils.SyncUtils;
import com.squareup.leakcanary.RefWatcher;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public abstract class AbstractActivity extends AppCompatActivity implements LifeCycle {

	protected static final String LOG_TAG = "ACTIVITY";

	private Toolbar toolbar;

	private List<OnLifeCycleListener> onLifeCycleListenerList;

	protected static void startActivity(AbstractActivity activity,
										Class<? extends AbstractActivity> activityClass,
										List<Pair<View, String>> sharedElements,
										Bundle extras) {
		Intent intent = new Intent(activity, activityClass);
		if (extras != null) {
			intent.putExtras(extras);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			ActivityOptions options;
			if (sharedElements == null) {
				sharedElements = new ArrayList<>();
			}
			Toolbar toolbar = activity.getToolbar();
			if (toolbar != null) {
				sharedElements.add(new Pair<>((View) toolbar, "toolbar"));
			}
			//noinspection unchecked
			Pair<View, String>[] pairs = new Pair[sharedElements.size()];
			pairs = sharedElements.toArray(pairs);
			//noinspection unchecked
			options = ActivityOptions.makeSceneTransitionAnimation(activity, pairs);
			activity.startActivity(intent, options.toBundle());
		} else {
			activity.startActivity(intent);
		}
	}

	public static String getExceptionError(@Nonnull Context context, @Nonnull Exception e) {
		if (e instanceof NetworkErrorException) {
			return context.getString(R.string.error_network_error);
		} else if (e instanceof ConnectException) {
			return context.getString(R.string.error_network_no_api_connection);
		} else if (e instanceof GoogleJsonResponseException) {
			Log.e(LOG_TAG, e.getMessage(), e);
			GoogleJsonResponseException ge = (GoogleJsonResponseException) e;
			if (ge.getDetails() != null) {
				return context.getString(R.string.error_unexpected, ge.getDetails().getMessage());
			} else {
				return context.getString(R.string.error_unknown, e.getMessage());
			}
		} else {
			Log.e(LOG_TAG, e.getMessage(), e);
			return context.getString(R.string.error_unknown, e.getMessage());
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
	protected void onPause() {
		super.onPause();
		AsyncCache.instance().onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		AsyncCache.instance().onResume(this);
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
		Snackbar.make(findViewById(R.id.container), getExceptionError(this, e),
					  Snackbar.LENGTH_LONG).show();
	}

	public void showSnackBarException(String message) {
		Snackbar.make(findViewById(R.id.container), message, Snackbar.LENGTH_LONG).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			AsyncCache.instance().onCreate(this);
		} else {
			AsyncCache.instance().onRecreate(this);
		}
	}

	public void setSupportActionBar(Toolbar toolbar) {
		this.toolbar = toolbar;
		super.setSupportActionBar(toolbar);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onDestroy() {
		AsyncCache.instance().onDestroy(this);
		super.onDestroy();
		RefWatcher refWatcher = Application.getRefWatcher(this);
		refWatcher.watch(this);
	}
}
