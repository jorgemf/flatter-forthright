package com.livae.ff.app.viewholders;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.Constants;
import com.livae.ff.app.R;

public class CountryViewHolder {

	private final StyleSpan boldSpan;

	private ImageView flag;

	private TextView countryText;

	private Constants.COUNTRY country;

	public CountryViewHolder(@NonNull View view) {
		flag = (ImageView) view.findViewById(R.id.flag_icon);
		countryText = (TextView) view.findViewById(R.id.country_text);
		boldSpan = new StyleSpan(Typeface.BOLD);
	}

	public Constants.COUNTRY getCountry() {
		return country;
	}

	public void setCountry(@NonNull Constants.COUNTRY country) {
		this.country = country;
		int flagRes = country.getCountryFlagResId();
		flag.setImageResource(flagRes);
		if (flagRes == 0) {
			flag.setVisibility(View.INVISIBLE);
		} else {
			flag.setVisibility(View.VISIBLE);
		}
		countryText.setText(country.getPhonePrefix());
	}

}
