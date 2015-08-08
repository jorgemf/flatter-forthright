package com.livae.ff.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.livae.ff.app.R;

import javax.annotation.Nonnull;

public abstract class EditTextDialogFragment extends DialogFragment
  implements DialogInterface.OnClickListener, TextWatcher, View.OnClickListener {

	private static final String ARGUMENT_MESSAGE = "ARGUMENT_MESSAGE";

	private static final String ARGUMENT_TITLE = "ARGUMENT_TITLE";

	private static final String ARGUMENT_MAX_CHARS = "ARGUMENT_MAX_CHARS";

	private static final String ARGUMENT_TEXT = "ARGUMENT_TEXT";

	private EditText editText;

	private ProgressBar progressBar;

	private int maxChars;

	public void show(Context context,
					 FragmentManager fragmentManager,
					 @StringRes int titleResId,
					 @StringRes int messageResId,
					 @IntegerRes int maximumChars,
					 String text) {
		Resources resources = context.getResources();
		show(fragmentManager, resources.getString(titleResId), resources.getString(messageResId),
			 resources.getInteger(maximumChars), text);
	}

	public void show(FragmentManager fragmentManager,
					 String title,
					 String messageRes,
					 int maximumChars,
					 String text) {
		Bundle arguments = new Bundle();
		arguments.putString(ARGUMENT_TITLE, title);
		arguments.putString(ARGUMENT_MESSAGE, messageRes);
		arguments.putInt(ARGUMENT_MAX_CHARS, maximumChars);
		arguments.putString(ARGUMENT_TEXT, text);
		setArguments(arguments);
		show(fragmentManager, null);
	}

	@Nonnull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle arguments = getArguments();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(arguments.getString(ARGUMENT_TITLE));
		builder.setMessage(arguments.getString(ARGUMENT_MESSAGE));
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_text, null);
		builder.setView(view);
		builder.setPositiveButton(R.string.ok, this);
		AlertDialog dialog = builder.create();
		editText = (EditText) view.findViewById(R.id.dialog_edit_text);
		progressBar = (ProgressBar) view.findViewById(R.id.dialog_progress);
		InputFilter[] filters = new InputFilter[1];
		maxChars = arguments.getInt(ARGUMENT_MAX_CHARS);
		filters[0] = new InputFilter.LengthFilter(maxChars);
		editText.setFilters(filters);

		editText.setText(arguments.getString(ARGUMENT_TEXT));
		editText.addTextChangedListener(this);
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogInterface) {
				AlertDialog dialog = (AlertDialog) dialogInterface;
				Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				button.setOnClickListener(EditTextDialogFragment.this);

				InputMethodManager keyboard;
				keyboard =
				  (InputMethodManager) getActivity().getSystemService(Context
																		.INPUT_METHOD_SERVICE);
				keyboard.showSoftInput(editText, 0);
				editText.selectAll();
			}
		});
		return dialog;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// nothing
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// nothing
	}

	@Override
	public void afterTextChanged(Editable editable) {
		String comment = editText.getText().toString();
		int length = comment.length();
		AlertDialog dialog = (AlertDialog) getDialog();
		Button okButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		okButton.setEnabled(length > 0);
	}

	@Override
	public void onClick(View v) {
		positiveButton();
	}

	@Override
	public void onClick(DialogInterface dialogInterface, int which) {
		switch (which) {
			case DialogInterface.BUTTON_NEUTRAL:
			case DialogInterface.BUTTON_NEGATIVE:
				dismiss();
				break;
			case DialogInterface.BUTTON_POSITIVE:
				positiveButton();
				break;
		}
	}

	private void positiveButton() {
		String text = editText.getText().toString().trim();
		int textLength = text.length();
		if (textLength > 0 && textLength < maxChars) {
			AlertDialog dialog = (AlertDialog) getDialog();
			dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
			dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
			setCancelable(false);
			progressBar.setVisibility(View.VISIBLE);
			editText.setEnabled(false);
			performAction(this, text);
		} else {
			editText.setText(text);
		}
	}

	protected abstract void performAction(EditTextDialogFragment dialog, String newText);

	public void retry() {
		AlertDialog dialog = (AlertDialog) getDialog();
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
		dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
		setCancelable(true);
		progressBar.setVisibility(View.GONE);
		editText.setEnabled(true);
	}

}
