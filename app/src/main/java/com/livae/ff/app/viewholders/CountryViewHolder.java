package com.livae.ff.app.viewholders;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.apphunt.app.Constants;
import com.livae.apphunt.app.R;

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
		flag.setImageResource(country.getCountryFlagResId());
		countryText.setText(country.getCountryStringResId());
	}

	public void highlightText(boolean isCurrentCountry, String searchString) {
		Resources resources = countryText.getResources();
		if (isCurrentCountry) {
			countryText.setTextColor(resources.getColor(R.color.black));
		} else {
			countryText.setTextColor(resources.getColor(R.color.grey_dark));
		}

		if (searchString != null && searchString.length() > 1) {
			String textCountry = countryText.getText().toString();
			SpannableString span = new SpannableString(textCountry);
			int queryLength = searchString.length();
			searchString = searchString.toLowerCase();
			String textCountryLoweCase = textCountry.toLowerCase();
			int index = textCountryLoweCase.indexOf(searchString);
			while (index >= 0) {
				span.setSpan(boldSpan, index, index + queryLength,
							 Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				index = textCountryLoweCase.indexOf(searchString, index + queryLength);
			}
			countryText.setText(span);
		}
	}
}
