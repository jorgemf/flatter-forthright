package com.livae.ff.app.fragment;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.Constants;
import com.livae.ff.app.R;
import com.livae.ff.app.activity.OnBoardingActivity;
import com.livae.ff.app.adapter.CountriesArrayAdapter;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.dialog.CountdownDialogFragment;
import com.livae.ff.app.dialog.ProgressDialogFragment;
import com.livae.ff.app.settings.Settings;
import com.livae.ff.app.task.TaskRegister;
import com.livae.ff.app.utils.PhoneUtils;
import com.livae.ff.app.utils.PhoneVerification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class OnBoardingVerifyNumberFragment extends AbstractFragment
  implements View.OnClickListener {

	private static final String LOG_TAG = "NUMBER_VERIFICATION";

	private static final String CODE_LIMIT = "#";

	private BroadcastReceiver smsReceiver;

	private View validateButton;

	private EditText phoneNumberEditText;

	private TextView phonePrefixEditText;

	private Spinner countryCodeSpinner;

	private CountriesArrayAdapter countriesAdapter;

	private CountdownDialogFragment countdownDialogFragment;

	private ProgressDialogFragment progressDialogFragment;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_on_boarding_validate_phone, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		validateButton = view.findViewById(R.id.button);
		validateButton.setOnClickListener(this);
		phoneNumberEditText = (EditText) view.findViewById(R.id.edit_text_phone);
		phonePrefixEditText = (TextView) view.findViewById(R.id.edit_text_prefix);
		countryCodeSpinner = (Spinner) view.findViewById(R.id.spinner_country);
		countriesAdapter = new CountriesArrayAdapter(getActivity());
		countryCodeSpinner.setAdapter(countriesAdapter);

		TelephonyManager tm;
		tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		String countryISO = tm.getSimCountryIso();
		String phoneNumber = tm.getLine1Number();
		Log.i(LOG_TAG, "Phone number: " + phoneNumber);
		Log.i(LOG_TAG, "Sim country ISO: " + countryISO);
		Constants.COUNTRY country = Constants.COUNTRY.US;
		try {
			country = Constants.COUNTRY.valueOf(countryISO.toUpperCase());
		} catch (Exception e) {
			Analytics.logAndReport("Unknown country ISO code: " + countryISO, false);
		}
		phoneNumberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
		List<Constants.COUNTRY> list = new ArrayList<>();
		list.addAll(Arrays.asList(Constants.COUNTRY.values()));
		// sort countries by name
		final HashMap<Constants.COUNTRY, String> countryNames = new HashMap<>();
		for (Constants.COUNTRY c : list) {
			countryNames.put(c, getString(c.getCountryStringResId()));
		}
		Collections.sort(list, new Comparator<Constants.COUNTRY>() {
			@Override
			public int compare(Constants.COUNTRY lhs, Constants.COUNTRY rhs) {
				return countryNames.get(lhs).compareTo(countryNames.get(rhs));
			}
		});
		countriesAdapter.setCountries(list);
		countryCodeSpinner.setSelection(countriesAdapter.getPosition(country));
		phonePrefixEditText.setText(country.getPhonePrefix());
		countryCodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Constants.COUNTRY country = countriesAdapter.getItem(position);
				phonePrefixEditText.setText(country.getPhonePrefix());
				verifyNumber();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				verifyNumber();
			}
		});
		phoneNumberEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				verifyNumber();
			}
		});
		if (phoneNumber != null) {
			String countryPrefix = country.getPhonePrefix().replace("+", "").replace("-", "");
			if (phoneNumber.startsWith(countryPrefix)) {
				phoneNumber = phoneNumber.substring(countryPrefix.length());
			}
			phoneNumberEditText.setText(phoneNumber);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		smsReceiver = new IncomingSms();
		IntentFilter intentFilter;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
		} else {
			intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		}
		getActivity().registerReceiver(smsReceiver, intentFilter);
		PhoneVerification phoneVerification = PhoneVerification.instance(getActivity());
		final Long date = phoneVerification.getDate();
		if (phoneVerification.getVerificationToken() != null &&
			date != null &&
			date > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)) {
			checkSmsConfirmation();
		}
		if (date != null &&
			date > System.currentTimeMillis() - Settings.PHONE_VERIFICATION_TRY_AGAIN_DELAY) {
			showCountDownDialog(System.currentTimeMillis() - date -
								Settings.PHONE_VERIFICATION_TRY_AGAIN_DELAY);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(smsReceiver);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button:
				pressedContinue();
				break;
		}
	}

	private void pressedContinue() {
		// hide keyboard
		View view = getActivity().getCurrentFocus();
		if (view != null) {
			InputMethodManager inputManager;
			inputManager = (InputMethodManager) getActivity()
												  .getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(),
												 InputMethodManager.HIDE_NOT_ALWAYS);
		}
		// get phone number
		final Phonenumber.PhoneNumber phoneNumber = getPhoneNumber();
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		final int selectedItemPosition = countryCodeSpinner.getSelectedItemPosition();
		final String country = countriesAdapter.getItem(selectedItemPosition).name().toUpperCase();
		String number = phoneUtil.formatOutOfCountryCallingNumber(phoneNumber, country);
		new AlertDialog.Builder(getActivity())
		  .setMessage(getString(R.string.verification_sms_dialog_confirmation, number))
		  .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int id) {
				  String phoneString =
					"+" + phoneNumber.getCountryCode() + phoneNumber.getNationalNumber();
				  Long phoneLong = Long.parseLong(phoneString);
				  PhoneVerification verification = PhoneVerification.instance(getActivity());
				  verification.setUserPhone(phoneLong);
				  Random random = new Random();
				  int verificationCode = random.nextInt(999999) + 1; // 6 digits
				  verification.setVerificationToken(verificationCode);
				  verification.setDate(System.currentTimeMillis());
				  Log.d(LOG_TAG, "Phone number: " + phoneString + "  verification code: " +
								 verificationCode);
				  sendVerificationSMS(phoneString, verificationCode);
			  }
		  }).setNegativeButton(R.string.no, null).setCancelable(false).show();
	}

	private void verifyNumber() {
		String prefix = countriesAdapter.getItem(countryCodeSpinner.getSelectedItemPosition())
										.getPhonePrefix();
		String number = phoneNumberEditText.getText().toString();
		validateButton.setEnabled(PhoneUtils.getMobileNumber(prefix + number, null) != null);
	}

	private Phonenumber.PhoneNumber getPhoneNumber() {
		String prefix = countriesAdapter.getItem(countryCodeSpinner.getSelectedItemPosition())
										.getPhonePrefix();
		String number = phoneNumberEditText.getText().toString();
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		Phonenumber.PhoneNumber phoneNumber = null;
		try {
			phoneNumber = phoneUtil.parse(prefix + number, "");
		} catch (NumberParseException e) {
			Analytics.logAndReport(e);
		}
		return phoneNumber;
	}

	private void sendVerificationSMS(final String phoneNumber, int code) {
		SmsManager sms = SmsManager.getDefault();
		//noinspection ConstantConditions,PointlessBooleanExpression
		final String message = getActivity().getString(R.string.verification_sms, code);
		Log.i(LOG_TAG, "SENT num: " + phoneNumber + " body: " + message);
		//noinspection PointlessBooleanExpression,PointlessBooleanExpression,ConstantConditions
		if (!BuildConfig.DEV && !BuildConfig.DEBUG) {
			sms.sendTextMessage(phoneNumber, null, message, null, null);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@TargetApi(Build.VERSION_CODES.KITKAT)
				@Override
				public void run() {
					checkSmsConfirmation(phoneNumber, message);
				}
			}, 3000);
		}
		showCountDownDialog(Settings.PHONE_VERIFICATION_TRY_AGAIN_DELAY);
	}

	private void showCountDownDialog(long countDown) {
		countdownDialogFragment = new CountdownDialogFragment();
		countdownDialogFragment.setMessage(getString(R.string.verification_sms_dialog), countDown);
		countdownDialogFragment.show(getFragmentManager(), "count_down_dialog");
	}

	private boolean checkSmsConfirmation() {
		boolean validated = false;
		Cursor cursor;
		final ContentResolver contentResolver = getActivity().getContentResolver();
		Uri uri;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			uri = Telephony.Sms.CONTENT_URI;
		} else {
			uri = Uri.parse("content://sms");
		}
		String columnDate;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			columnDate = Telephony.Sms.DATE;
		} else {
			columnDate = "date";
		}
		String columnType;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			columnType = Telephony.Sms.TYPE;
		} else {
			columnType = "type";
		}
		String typeInbox;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			typeInbox = Integer.toString(Telephony.Sms.MESSAGE_TYPE_INBOX);
		} else {
			typeInbox = "1";
		}
		String oldestDate = Long.toString(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
		cursor = contentResolver.query(uri, null, columnDate + ">? AND " + columnType + "=?",
									   new String[]{oldestDate, typeInbox}, null);
		if (cursor.moveToFirst()) {
			int counter = 0;
			do {
				String columnBody;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					columnBody = Telephony.Sms.BODY;
				} else {
					columnBody = "body";
				}
				String columnPhone;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					columnPhone = Telephony.Sms.ADDRESS;
				} else {
					columnPhone = "address";
				}
				String message = cursor.getString(cursor.getColumnIndex(columnBody));
				String phoneNumber = cursor.getString(cursor.getColumnIndex(columnPhone));
				validated = checkSmsConfirmation(phoneNumber, message);
				counter++;
			} while (cursor.moveToNext() && counter <= 10 && !validated);
		}
		cursor.close();
		return validated;
	}

	private boolean checkSmsConfirmation(String phoneNumber, String message) {
		boolean valid;
		PhoneVerification phoneVerification = PhoneVerification.instance(getActivity());
		String codeString =
		  CODE_LIMIT + phoneVerification.getVerificationToken().toString() + CODE_LIMIT;
		String phoneToVerify = "+" + phoneVerification.getUserPhone();
		valid = message != null && phoneNumber != null && message.contains(codeString) &&
				phoneNumber.equals(phoneToVerify);
		Log.i(LOG_TAG, "VERIFY SMS: " + phoneNumber + " body: " + message + " result: " + valid);
		if (valid) {
			phoneValidated();
		}
		return valid;
	}

	private void phoneValidated() {
		if (countdownDialogFragment != null) {
			countdownDialogFragment.dismiss();
			countdownDialogFragment = null;
		}
		if (progressDialogFragment == null) {
			progressDialogFragment = new ProgressDialogFragment();
			progressDialogFragment.setMessage(getString(R.string.signing_in));
		}
		progressDialogFragment.show(getFragmentManager(), "progress_dialog");
		TaskRegister task = new TaskRegister();
		PhoneVerification phoneVerification = PhoneVerification.instance(getActivity());
		task.execute(phoneVerification.getUserPhone(), new Callback<Long, Void>() {
			@Override
			public void onComplete(CustomAsyncTask<Long, Void> task, Long phone, Void aVoid) {
				progressDialogFragment.dismiss();
				if (isResumed() && getActivity() != null) {
					((OnBoardingActivity) getActivity()).nextStep();
				}
			}

			@Override
			public void onError(CustomAsyncTask<Long, Void> task, Long phone, Exception e) {
				progressDialogFragment.dismiss();
				if (isResumed() && getActivity() != null) {
					new AlertDialog.Builder(getActivity())
					  .setMessage(getString(R.string.error_signing_in_retry, e.getMessage()))
					  .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						  public void onClick(DialogInterface dialog, int id) {
							  phoneValidated();
						  }
					  }).setNegativeButton(R.string.cancel, null).setCancelable(false).show();
				}
			}
		});
	}

	class IncomingSms extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG, "Received sms");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
				for (SmsMessage currentMessage : messages) {
					String phoneNumber = currentMessage.getOriginatingAddress();
					String message = currentMessage.getDisplayMessageBody();
					if (checkSmsConfirmation(phoneNumber, message)) {
						break;
					}
				}
			} else {
				final Bundle bundle = intent.getExtras();
				if (bundle != null) {
					try {
						final Object[] pdusObj = (Object[]) bundle.get("pdus");
						boolean validated = false;
						for (int i = 0; i < pdusObj.length && !validated; i++) {
							SmsMessage currentMessage = SmsMessage
														  .createFromPdu((byte[]) pdusObj[i]);
							String phoneNumber = currentMessage.getOriginatingAddress();
							String message = currentMessage.getDisplayMessageBody();
							validated = checkSmsConfirmation(phoneNumber, message);
						}
					} catch (Exception e) {
						Analytics.logAndReport(e);
					}
				}
			}
		}
	}
}
