package com.livae.ff.app.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.Constants;
import com.livae.ff.app.R;

import javax.annotation.Nonnull;

public class CountryViewHolder {

	private ImageView flag;

	private TextView countryName;

	private Constants.COUNTRY country;

	public CountryViewHolder(@Nonnull View view) {
		flag = (ImageView) view.findViewById(R.id.flag_icon);
		countryName = (TextView) view.findViewById(R.id.country_name);
	}

	public Constants.COUNTRY getCountry() {
		return country;
	}

	public void setCountry(@Nonnull Constants.COUNTRY country, boolean prefix) {
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
