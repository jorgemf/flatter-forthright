package com.livae.ff.app.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.livae.ff.app.Constants;
import com.livae.ff.app.R;
import com.livae.ff.app.viewholders.CountryViewHolder;

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
		return getView(position, convertView, parent, R.layout.item_country_selected, false);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent, R.layout.item_country, true);
	}

	public View getView(int position, View convertView, ViewGroup parent, @LayoutRes int layout,
						boolean prefix) {
		View view;

		CountryViewHolder viewHolder;
		if (convertView == null) {
			view = inflater.inflate(layout, parent, false);
			viewHolder = new CountryViewHolder(view);
			view.setTag(viewHolder);
		} else {
			view = convertView;
			viewHolder = (CountryViewHolder) view.getTag();
		}
		viewHolder.setCountry(getItem(position), prefix);

		return view;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public void setCountries(List<Constants.COUNTRY> countries) {
		// sort countries by phone prefix
//		Collections.sort(countries, new Comparator<Constants.COUNTRY>() {
//			@Override
//			public int compare(Constants.COUNTRY lhs, Constants.COUNTRY rhs) {
//				return lhs.getPhonePrefix().compareTo(rhs.getPhonePrefix());
//			}
//		});
		// add countries
		clear();
		this.countries = countries;
		addAll(this.countries);
		notifyDataSetChanged();
	}

}
