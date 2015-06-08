package com.livae.ff.app.fragment;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
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
import android.widget.EditText;
import android.widget.Spinner;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.Constants;
import com.livae.ff.app.R;
import com.livae.ff.app.adapter.CountriesArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OnBoardingVerifyNumberFragment extends AbstractFragment
  implements View.OnClickListener {

	private static final String LOG_TAG = "NUMBER_VERIFICATION";

	private static final String CODE_LIMIT = "#";

	private BroadcastReceiver smsReceiver;

	private Long smsSent;

	private View validateButton;

	private EditText phoneNumberEditText;

	private Spinner countryCodeSpinner;

	private CountriesArrayAdapter countriesAdapter;

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
		countryCodeSpinner = (Spinner) view.findViewById(R.id.spinner_country);
		countriesAdapter = new CountriesArrayAdapter(getActivity());
		countryCodeSpinner.setAdapter(countriesAdapter);

		TelephonyManager tm;
		tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		String countryISO = tm.getSimCountryIso();
		String phoneNumber = tm.getLine1Number();
		Log.i(LOG_TAG, "Phone number: " + phoneNumber);
		Log.i(LOG_TAG, "Sim country ISO: " + countryISO);
		if (phoneNumber != null) {
			phoneNumberEditText.setText(phoneNumber);
		}
		Constants.COUNTRY country = Constants.COUNTRY.US;
		try {
			country = Constants.COUNTRY.valueOf(countryISO.toUpperCase());
		} catch (Exception e) {
			Analytics.logAndReport("Unknown country ISO code: " + countryISO, false);
		}
		List<Constants.COUNTRY> list = new ArrayList<>();
		list.addAll(Arrays.asList(Constants.COUNTRY.values()));
		countriesAdapter.setCountries(list);
		countryCodeSpinner.setSelection(countriesAdapter.getPosition(country));
		phoneNumberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
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
		verifyNumber();
	}

	@Override
	public void onResume() {
		super.onResume();
		smsReceiver = new IncomingSms();
		IntentFilter intentFilter = new IntentFilter();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
		} else {
			intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		}
		getActivity().registerReceiver(smsReceiver, intentFilter);
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
				smsSent = System.currentTimeMillis();
//				sendSMS();
				break;
		}
	}

	private void verifyNumber() {
		String prefix = countriesAdapter.getItem(countryCodeSpinner.getSelectedItemPosition())
										.getPhonePrefix();
		String number = phoneNumberEditText.getText().toString();
		boolean valid = false;
		try {
			PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
			Phonenumber.PhoneNumber phoneNumber = null;
			phoneNumber = phoneUtil.parseAndKeepRawInput(prefix + number, null);
			PhoneNumberUtil.PhoneNumberType numberType = phoneUtil.getNumberType(phoneNumber);
			valid = phoneUtil.isPossibleNumber(phoneNumber) &&
					phoneUtil.isValidNumber(phoneNumber) &&
					numberType == PhoneNumberUtil.PhoneNumberType.MOBILE;
		} catch (NumberParseException e) {
			Analytics.logAndReport(e);
		}
		validateButton.setEnabled(valid);
	}

	private void sendSMS(String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
		//noinspection ConstantConditions,PointlessBooleanExpression
		if (BuildConfig.DEBUG || BuildConfig.DEV) {
			phoneNumber = "5556"; // emulator number
		}
		Log.i(LOG_TAG, "SENT num: " + phoneNumber + " body: " + message);
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}

	private boolean checkSmsContainsCode(Integer code) {
		boolean contains = false;
		if (code != null) {
			Cursor cursor;
			final ContentResolver contentResolver = getActivity().getContentResolver();
			Uri uri;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				uri = Telephony.Sms.CONTENT_URI;
			} else {
				uri = Uri.parse("content://sms");
			}
			cursor = contentResolver.query(uri, null, null, null, null);
			if (cursor.moveToFirst()) {
				int counter = 0;
				String codeString = CODE_LIMIT + code.toString() + CODE_LIMIT;
				do {
					String column;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						column = Telephony.Sms.BODY;
					} else {
						column = "body";
					}
					String sms = cursor.getString(cursor.getColumnIndex(column));
					contains = sms.contains(codeString);
					counter++;
				} while (cursor.moveToNext() && counter <= 3 && !contains);
			}
			cursor.close();
		}
		return contains;
	}

	class IncomingSms extends BroadcastReceiver {

		final SmsManager sms = SmsManager.getDefault();

		@Override
		public void onReceive(Context context, Intent intent) {
			final Bundle bundle = intent.getExtras();
			if (bundle != null) {
				try {
					final Object[] pdusObj = (Object[]) bundle.get("pdus");
					for (int i = 0; i < pdusObj.length; i++) {
						SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
						String phoneNumber = currentMessage.getDisplayOriginatingAddress();
						String message = currentMessage.getDisplayMessageBody();

						Log.i(LOG_TAG, "RECEIVED num: " + phoneNumber + " body: " + message);
						if (checkSmsContainsCode(Application.appUser().getVerificationToken())) {
							// TODO
						}
					}
				} catch (Exception e) {
					Analytics.logAndReport(e);
				}
			}
		}
	}
}
