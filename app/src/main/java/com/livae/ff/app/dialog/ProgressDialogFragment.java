package com.livae.ff.app.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {

	private static final String ARGUMENT_MESSAGE = "ARGUMENT_MESSAGE";

	public void setMessage(String message) {
		Bundle bundle = new Bundle();
		bundle.putString(ARGUMENT_MESSAGE, message);
		setArguments(bundle);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		ProgressDialog progressDialog = new ProgressDialog(getActivity());
		progressDialog.setIndeterminate(true);
		String message = getArguments().getString(ARGUMENT_MESSAGE, null);
		if (message != null) {
			progressDialog.setMessage(message);
		}
		progressDialog.setCancelable(false);
		setCancelable(false);
		return progressDialog;
	}

}
