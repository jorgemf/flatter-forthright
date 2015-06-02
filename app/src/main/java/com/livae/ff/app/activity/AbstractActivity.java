package com.livae.ff.app.activity;

import android.accounts.NetworkErrorException;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.livae.ff.app.R;

import java.net.ConnectException;

public abstract class AbstractActivity extends AppCompatActivity {

	protected static final String LOG_TAG = "ACTIVITY";

	private Toolbar toolbar;

	public void showSnackBarException(@NonNull Exception e) {
//		showSnackBar(getSnackBarExceptionError(e));
	}

	public void showSnackBarException(@NonNull Exception e,
									  @NonNull View.OnClickListener retryListener) {
//		showSnackBar(getSnackBarExceptionError(e), R.string.button_retry, retryListener, false);
	}

	private String getSnackBarExceptionError(@NonNull Exception e) {
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
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void setSupportActionBar(Toolbar toolbar) {
		this.toolbar = toolbar;
		super.setSupportActionBar(toolbar);
	}

}