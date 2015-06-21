package com.livae.ff.app.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.Constants;
import com.livae.ff.app.R;

public class CountryViewHolder {

	private ImageView flag;

	private TextView countryName;

	private Constants.COUNTRY country;

	public CountryViewHolder(@NonNull View view) {
		flag = (ImageView) view.findViewById(R.id.flag_icon);
		countryName = (TextView) view.findViewById(R.id.country_name);
	}

	public Constants.COUNTRY getCountry() {
		return country;
	}

	public void setCountry(@NonNull Constants.COUNTRY country, boolean prefix) {
		this.country = country;
		int flagRes = country.getCountryFlagResId();
		flag.setImageResource(flagRes);
		if (flagRes == 0) {
			flag.setVisibility(View.INVISIBLE);
		} else {
			flag.setVisibility(View.VISIBLE);
		}
		if (prefix) {
			String name = countryName.getContext().getString(country.getCountryStringResId());
			countryName.setText(name + " (" + country.getPhonePrefix() + ")");
		} else {
			countryName.setText(country.getCountryStringResId());
		}
	}

}
