package com.livae.ff.app.activity;

import android.accounts.NetworkErrorException;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.livae.apphunt.app.R;
import com.livae.apphunt.app.Settings;
import com.livae.apphunt.app.dialog.LoginGoogleDialogFragment;
import com.livae.apphunt.app.listener.AnimatorListener;

import java.net.ConnectException;

public abstract class AbstractActivity extends ActionBarActivity {

	protected static final String LOG_TAG = "ACTIVITY";

	private static final String STATE_SNACK_BAR_VISIBLE = "STATE_SNACK_BAR_VISIBLE";

	private static final String STATE_SNACK_BAR_AUTOHIDE = "STATE_SNACK_BAR_AUTOHIDE";

	private Toolbar toolbar;

	private boolean snackBarAutohide;

	private AnimatorListener snackBarShowListener = new AnimatorListener() {

		@Override
		public void onAnimationEnd(Animator animation) {
			if (isEnabled()) {
				if (snackBarAutohide) {
					setSnackBarAutohideThread();
				}
			}
		}

		@Override
		public void onAnimationStart(Animator animation) {
			if (isEnabled()) {
				snackBar.setVisibility(View.VISIBLE);
			}
		}

	};

	private View snackBar;

	private TextView snackBarText;

	private Button snackBarButton;

	private Runnable snackBarHideThread;

	private ValueAnimator snackBarValueAnimator;

	private AnimatorListener snackBarHideListener = new AnimatorListener() {

		@Override
		public void onAnimationEnd(Animator animation) {
			if (isEnabled()) {
				snackBar.setVisibility(View.GONE);
			}
		}
	};

	protected void setSnackBarView(View snackBarView) {
		snackBar = snackBarView;
		snackBarText = (TextView) snackBarView.findViewById(R.id.snak_text);
		snackBarButton = (Button) snackBarView.findViewById(R.id.snack_button_action);
		snackBarButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.snack_button_action:
						Object tag = snackBar.getTag();
						if (tag instanceof View.OnClickListener) {
							((View.OnClickListener) tag).onClick(v);
						}
						hideSnackBar();
						break;
				}
			}
		});
	}

	private void hideSnackBar() {
		if (snackBar != null) {
			int height = snackBar.getHeight();
			setSnackBarHeightAnimator(height, 0);
			snackBarHideListener.setEnable(true);
			snackBarValueAnimator.start();
		}
	}

	public void showSnackBar(@StringRes int textResId) {
		showSnackBar(getString(textResId), null, null, true);
	}

	public void showSnackBar(@StringRes int textResId, final boolean autoHide) {
		showSnackBar(getString(textResId), null, null, autoHide);
	}

	public void showSnackBar(CharSequence text, final boolean autoHide) {
		showSnackBar(text, null, null, autoHide);
	}

	public void showSnackBar(CharSequence text) {
		showSnackBar(text, null, null, true);
	}

	public void showSnackBar(@StringRes int textResId, @StringRes int buttonResId,
							 @NonNull View.OnClickListener onClickListener, boolean autoHide) {
		showSnackBar(getString(textResId), buttonResId, onClickListener, autoHide);
	}

	public void showSnackBar(CharSequence text, @StringRes int buttonResId,
							 @NonNull View.OnClickListener onClickListener, boolean autoHide) {
		showSnackBar(text, getString(buttonResId), onClickListener, autoHide);
	}

	public void showSnackBar(final CharSequence text, final CharSequence buttonText,
							 final View.OnClickListener onClickListener, final boolean autoHide) {
		snackBar.setTag(null);
		snackBarButton.setVisibility(View.GONE);

		if (buttonText != null) {
			snackBarButton.setVisibility(View.VISIBLE);
			snackBarButton.setText(buttonText);
		}
		if (onClickListener != null) {
			snackBar.setTag(onClickListener);
		}
		snackBarText.setText(text);
		snackBar.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		int measuredHeight = snackBar.getMeasuredHeight();
		final int lineCount = snackBarText.getLineCount();
		if (lineCount == 1 || lineCount == 0) { // hack
			measuredHeight = getResources().getDimensionPixelSize(R.dimen.snackbar_small);
		} else if (lineCount == 2) {
			measuredHeight = getResources().getDimensionPixelSize(R.dimen.snackbar_big);
		}
		setSnackBarHeightAnimator(0, measuredHeight);
		snackBarAutohide = autoHide;
		snackBarShowListener.setEnable(true);
		snackBarValueAnimator.start();
		snackBar.post(new Runnable() { // hack
			@Override
			public void run() {
				int newLineCount = snackBarText.getLineCount();
				if (lineCount != snackBarText.getLineCount()) {
					int measuredHeight;
					if (newLineCount == 2) {
						measuredHeight = getResources().getDimensionPixelSize(R.dimen.snackbar_big);
					} else {
						measuredHeight = getResources()
										   .getDimensionPixelSize(R.dimen.snackbar_small);
					}
					setSnackBarHeightAnimator(0, measuredHeight);
					snackBarValueAnimator.start();
				}
			}
		});
	}

	private void setSnackBarAutohideThread() {
		int snackBarAutoHideTime = getResources().getInteger(R.integer.snack_bar_autohide_delay);
		if (snackBarHideThread == null) {
			snackBarHideThread = new Runnable() {
				@Override
				public void run() {
					hideSnackBar();
				}
			};
		}
		snackBar.removeCallbacks(snackBarHideThread);
		snackBar.postDelayed(snackBarHideThread, snackBarAutoHideTime);
	}

	public void showSnackBarException(@NonNull Exception e) {
		showSnackBar(getSnackBarExceptionError(e));
	}

	public void showSnackBarException(@NonNull Exception e,
									  @NonNull View.OnClickListener retryListener) {
		showSnackBar(getSnackBarExceptionError(e), R.string.button_retry, retryListener, false);
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
		if (snackBar != null) {
			if (savedInstanceState.getBoolean(STATE_SNACK_BAR_VISIBLE, false)) {
				snackBar.setVisibility(View.VISIBLE);
				if (savedInstanceState.getBoolean(STATE_SNACK_BAR_AUTOHIDE, false)) {
					setSnackBarAutohideThread();
				}
			}
		}
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

	private void setSnackBarHeightAnimator(int start, int end) {
		snackBar.removeCallbacks(snackBarHideThread);
		if (snackBarValueAnimator != null) {
			snackBarValueAnimator.cancel();
		} else {
			snackBarValueAnimator = new ValueAnimator();
			snackBarValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator valueAnimator) {
					int value = (Integer) valueAnimator.getAnimatedValue();
					ViewGroup.LayoutParams layoutParams = snackBar.getLayoutParams();
					layoutParams.height = value;
					snackBar.setLayoutParams(layoutParams);
					snackBarValueAnimator = null;
				}
			});
			snackBarValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
			int time = getResources().getInteger(android.R.integer.config_mediumAnimTime);
			snackBarValueAnimator.setDuration(time);
			snackBarValueAnimator.addListener(snackBarHideListener);
			snackBarValueAnimator.addListener(snackBarShowListener);
		}
		snackBarHideListener.setEnable(false);
		snackBarShowListener.setEnable(false);
		snackBarValueAnimator.setIntValues(start, end);
	}

	public Toolbar getToolbar() {
		return toolbar;
	}

	public void setSupportActionBar(Toolbar toolbar) {
		this.toolbar = toolbar;
		super.setSupportActionBar(toolbar);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			finishAfterTransition();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Settings.RequestCode.GOOGLE_SIGN_IN) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment f = fragmentManager.findFragmentByTag(LoginGoogleDialogFragment.TAG);
			DialogFragment dialog = (DialogFragment) f;
			dialog.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (snackBar != null && snackBar.getTag() == null) {
			outState.putBoolean(STATE_SNACK_BAR_VISIBLE, snackBar.getVisibility() == View.VISIBLE);
			outState.putBoolean(STATE_SNACK_BAR_AUTOHIDE, snackBarAutohide);
		}
	}
}