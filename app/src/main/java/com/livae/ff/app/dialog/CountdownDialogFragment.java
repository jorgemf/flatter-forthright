package com.livae.ff.app.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import com.livae.ff.app.R;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public class CountdownDialogFragment extends DialogFragment {

	private static final String ARGUMENT_MESSAGE = "ARGUMENT_MESSAGE";

	private static final String ARGUMENT_COUNTDOWN = "ARGUMENT_COUNTDOWN";

	private CountDownTimer timer;

	public void setMessage(String message, long countdown) {
		Bundle bundle = new Bundle();
		bundle.putString(ARGUMENT_MESSAGE, message);
		bundle.putLong(ARGUMENT_COUNTDOWN, countdown);
		setArguments(bundle);
	}

	@Nonnull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog alertDialog =
		  new AlertDialog.Builder(getActivity()).setView(R.layout.dialog_countdown).create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				String message = getArguments().getString(ARGUMENT_MESSAGE, null);
				((TextView) alertDialog.findViewById(R.id.text)).setText(message);
				final TextView countdown = (TextView) alertDialog.findViewById(R.id.count_down);
				long time = getArguments().getLong(ARGUMENT_COUNTDOWN, 0);
				timer = new CountDownTimer(time, 1000) {

					public void onTick(long millisUntilFinished) {
						long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
						long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
						countdown.setText(String.format("%2d:%02d", minutes, seconds));
					}

					public void onFinish() {
						dismiss();
					}
				}.start();
			}
		});
		setCancelable(false);
		return alertDialog;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (timer != null) {
			timer.cancel();
		}
	}
}
