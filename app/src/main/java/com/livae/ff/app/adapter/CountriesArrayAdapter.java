package com.livae.ff.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.livae.ff.app.Constants;
import com.livae.ff.app.R;
import com.livae.ff.app.viewholders.CountryViewHolder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CountriesArrayAdapter extends ArrayAdapter<Constants.COUNTRY> {

	private final LayoutInflater inflater;

	private List<Constants.COUNTRY> countries;

	public CountriesArrayAdapter(Context context) {
		super(context, R.layout.item_country);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;

		CountryViewHolder viewHolder;
		if (convertView == null) {
			view = inflater.inflate(R.layout.item_country, parent, false);
			viewHolder = new CountryViewHolder(view);
			view.setTag(viewHolder);
		} else {
			view = convertView;
			viewHolder = (CountryViewHolder) view.getTag();
		}
		viewHolder.setCountry(getItem(position));

		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public void setCountries(List<Constants.COUNTRY> countries) {
		// sort countries by phone prefix
		Collections.sort(countries, new Comparator<Constants.COUNTRY>() {
			@Override
			public int compare(Constants.COUNTRY lhs, Constants.COUNTRY rhs) {
				return lhs.getPhonePrefix().compareTo(rhs.getPhonePrefix());
			}
		});
		// add countries
		clear();
		this.countries = countries;
		addAll(this.countries);
		notifyDataSetChanged();
	}

}
