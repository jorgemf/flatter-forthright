package com.livae.ff.app.ui.fragment;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
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
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.listener.LifeCycle;
import com.livae.ff.app.service.SMSVerificationService;
import com.livae.ff.app.settings.Settings;
import com.livae.ff.app.task.TaskRegister;
import com.livae.ff.app.ui.activity.OnBoardingActivity;
import com.livae.ff.app.ui.adapter.CountriesArrayAdapter;
import com.livae.ff.app.ui.dialog.CountdownDialogFragment;
import com.livae.ff.app.ui.dialog.ProgressDialogFragment;
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

	private static final String INTENT_SMS_SEND = "com.livae.ff.app.INTENT_SMS_SEND";

	private static final String INTENT_SMS_DELIVER = "com.livae.ff.app.INTENT_SMS_DELIVER";

	private static final String LOG_TAG = "NUMBER_VERIFICATION";

	private BroadcastReceiver smsReceiver;

	private BroadcastReceiver smsSend;

	private BroadcastReceiver smsDeliver;

	private BroadcastReceiver smsVerified;

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
		TextView text = (TextView) view.findViewById(R.id.text_content);
		text.setMovementMethod(LinkMovementMethod.getInstance());
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
		PhoneVerification phoneVerification = PhoneVerification.instance(getActivity());
		final Long date = phoneVerification.getDate();
		if (phoneVerification.getVerificationToken() != null &&
			date != null &&
			date > System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)) {
			if (SMSVerificationService.checkSmsConfirmation(getActivity())) {
				phoneValidated();
			}
		}
		if (date != null &&
			date > System.currentTimeMillis() - Settings.PHONE_VERIFICATION_TRY_AGAIN_DELAY) {
			showCountDownDialog(System.currentTimeMillis() - date -
								Settings.PHONE_VERIFICATION_TRY_AGAIN_DELAY);
			createSMSBroadcastReceivers();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		final FragmentActivity activity = getActivity();
		if (smsReceiver != null) {
			activity.unregisterReceiver(smsReceiver);
			smsReceiver = null;
		}
		if (smsVerified != null) {
			activity.unregisterReceiver(smsVerified);
			smsVerified = null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		final FragmentActivity activity = getActivity();
		if (smsSend != null) {
			activity.unregisterReceiver(smsSend);
			smsSend = null;
		}
		if (smsDeliver != null) {
			activity.unregisterReceiver(smsDeliver);
			smsDeliver = null;
		}
	}

	private void createSMSBroadcastReceivers() {
		IntentFilter intentFilter;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			intentFilter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
		} else {
			intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		}
		smsReceiver = new SMSVerificationService.IncomingSms();
		final FragmentActivity activity = getActivity();
		activity.registerReceiver(smsReceiver, intentFilter);
		smsVerified = new SMSVerifiedReceiver();
		IntentFilter intentFilterVerified = new IntentFilter();
		intentFilterVerified.addAction(SMSVerificationService.INTENT_SMS_VERIFIED);
		intentFilterVerified.addAction(SMSVerificationService.INTENT_SMS_ERROR);
		activity.registerReceiver(smsVerified, intentFilterVerified);
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
			inputManager =
			  (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(),
												 InputMethodManager.HIDE_NOT_ALWAYS);
		}
		// get phone number
		final Phonenumber.PhoneNumber phoneNumber = getPhoneNumber();
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		final int selectedItemPosition = countryCodeSpinner.getSelectedItemPosition();
		final String country = countriesAdapter.getItem(selectedItemPosition).name().toUpperCase();
		String number = phoneUtil.formatOutOfCountryCallingNumber(phoneNumber, country);
		DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String phoneString = Long.toString(phoneNumber.getCountryCode()) +
									 Long.toString(phoneNumber.getNationalNumber());
				Long phoneLong = Long.parseLong(phoneString);
				PhoneVerification verification = PhoneVerification.instance(getActivity());
				verification.setUserPhone(phoneLong);
				generateCodeAndSendSMS();
			}
		};
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(getString(R.string.verification_sms_dialog_confirmation, number))
			   .setPositiveButton(R.string.yes, onClickListener)
			   .setNegativeButton(R.string.no, null)
			   .setCancelable(false)
			   .show();
	}

	private void generateCodeAndSendSMS() {
		PhoneVerification verification = PhoneVerification.instance(getActivity());
		Long phoneLong = verification.getUserPhone();
		String phoneString = "+" + phoneLong.toString();
		Random random = new Random();
		int verificationCode = random.nextInt(999999) + 1; // 6 digits
		verification.setVerificationToken(verificationCode);
		verification.setDate(System.currentTimeMillis());
		Log.d(LOG_TAG, "Phone number: " + phoneString + "  verification code: " +
					   verificationCode);
		sendVerificationSMS(phoneString, verificationCode);
	}

	private void verifyNumber() {
		String prefix =
		  countriesAdapter.getItem(countryCodeSpinner.getSelectedItemPosition()).getPhonePrefix();
		String number = phoneNumberEditText.getText().toString();
		validateButton.setEnabled(PhoneUtils.getMobileNumber(prefix + number, null) != null);
	}

	private Phonenumber.PhoneNumber getPhoneNumber() {
		String prefix =
		  countriesAdapter.getItem(countryCodeSpinner.getSelectedItemPosition()).getPhonePrefix();
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
		//noinspection ConstantConditions,PointlessBooleanExpression
		FragmentActivity context = getActivity();
		final String message = context.getString(R.string.verification_sms, code);
		Log.i(LOG_TAG, "SENT num: " + phoneNumber + " body: " + message);
		//noinspection PointlessBooleanExpression,PointlessBooleanExpression,ConstantConditions
		if (!BuildConfig.DEV && !BuildConfig.DEBUG) {
			// register the broadcasts receivers for sms received and sms verified
			createSMSBroadcastReceivers();
			// register broadcast receivers
			final FragmentActivity activity = getActivity();
			if (smsSend == null) {
				smsSend = new SMSVerificationService.SmsSendBroadcastReceiver();
				activity.registerReceiver(smsSend, new IntentFilter(INTENT_SMS_SEND));
			}
			if (smsDeliver == null) {
				smsDeliver = new SMSVerificationService.SmsDeliveredBroadcastReceiver();
				activity.registerReceiver(smsDeliver, new IntentFilter(INTENT_SMS_DELIVER));
			}
			// prepare pending intents
			final Intent sentI = new Intent(INTENT_SMS_SEND);
			final PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentI, 0);
			final Intent deliveredI = new Intent(INTENT_SMS_DELIVER);
			final PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, deliveredI,
																		 0);
			// send sms
			SmsManager sms = SmsManager.getDefault();
			Analytics.event(Analytics.Category.SMS, Analytics.Action.SMS_VERIFICATION,
							phoneNumber.substring(0, 5) + "…");
			sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
		} else {
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (SMSVerificationService.checkSmsConfirmation(getActivity(), phoneNumber,
																	message)) {
						phoneValidated();
					}
				}
			}, 2000);
		}
		showCountDownDialog(Settings.PHONE_VERIFICATION_TRY_AGAIN_DELAY);
	}

	private void showCountDownDialog(long countDown) {
		countdownDialogFragment = new CountdownDialogFragment();
		countdownDialogFragment.setMessage(getString(R.string.verification_sms_dialog), countDown);
		countdownDialogFragment.show(getFragmentManager(), "count_down_dialog");
	}

	private void phoneValidated() {
		Analytics.event(Analytics.Category.SMS, Analytics.Action.SMS_RECEIVED);
		if (countdownDialogFragment != null) {
			countdownDialogFragment.dismiss();
			countdownDialogFragment = null;
		}
		if (progressDialogFragment == null) {
			progressDialogFragment = new ProgressDialogFragment();
			progressDialogFragment.setMessage(getString(R.string.signing_in));
		}
		progressDialogFragment.show(getFragmentManager(), "progress_dialog");
		TaskRegister task = new TaskRegister(this);
		PhoneVerification phoneVerification = PhoneVerification.instance(getActivity());
		task.execute(phoneVerification.getUserPhone(), new Callback<LifeCycle, Long, Void>() {
			@Override
			public void onComplete(@NonNull LifeCycle lifeCycle, Long phone, Void aVoid) {
				OnBoardingVerifyNumberFragment f = (OnBoardingVerifyNumberFragment) lifeCycle;
				f.progressDialogFragment.dismiss();
				((OnBoardingActivity) f.getActivity()).nextStep();
			}

			@Override
			public void onError(@NonNull LifeCycle lifeCycle, Long phone, Exception e) {
				final OnBoardingVerifyNumberFragment f = (OnBoardingVerifyNumberFragment)
														   lifeCycle;
				f.progressDialogFragment.dismiss();
				final AlertDialog.Builder builder = new AlertDialog.Builder(f.getActivity());
				builder.setMessage(f.getString(R.string.error_signing_in_retry, e.getMessage()))
					   .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						   public void onClick(DialogInterface dialog, int id) {
							   f.phoneValidated();
						   }
					   })
					   .setNegativeButton(R.string.cancel, null)
					   .setCancelable(false)
					   .show();
			}
		});
	}

	class SMSVerifiedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
				case SMSVerificationService.INTENT_SMS_VERIFIED:
					int code = intent.getIntExtra(SMSVerificationService.EXTRA_CODE, -1);
					PhoneVerification phoneVerification = PhoneVerification.instance(getActivity
																					   ());
					if (code == phoneVerification.getVerificationToken()) {
						phoneValidated();
					}
					break;
				case SMSVerificationService.INTENT_SMS_ERROR:
					if (progressDialogFragment != null) {
						progressDialogFragment.dismiss();
					}
					if (isResumed() && getActivity() != null) {
						final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setMessage(getString(R.string.error_sms_not_sent))
							   .setPositiveButton(R.string.ok,
												  new DialogInterface.OnClickListener() {
													  public void onClick(DialogInterface dialog,
																		  int id) {
														  generateCodeAndSendSMS();
													  }
												  })
							   .setNegativeButton(R.string.cancel, null)
							   .show();
					}
					break;
			}
		}
	}

}
