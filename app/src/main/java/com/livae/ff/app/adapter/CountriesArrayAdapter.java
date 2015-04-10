package com.livae.ff.app.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.livae.ff.app.Constants;
import com.livae.ff.app.R;
import com.livae.ff.app.viewholders.CountryViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CountriesArrayAdapter extends ArrayAdapter<Constants.COUNTRY> {

	private final LayoutInflater inflater;

	private Constants.COUNTRY currentCountry;

	private String searchString;

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
		viewHolder.highlightText(currentCountry != null &&
								 currentCountry == viewHolder.getCountry(), searchString);

		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

	@Override
	public Filter getFilter() {
		return new ProxyFilter();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public void setCountries(List<Constants.COUNTRY> countries, Constants.COUNTRY userCountry) {
		// sort countries alphabetically
		final HashMap<Constants.COUNTRY, String> map = new HashMap<>();
		Resources resources = getContext().getResources();
		for (Constants.COUNTRY country : countries) {
			map.put(country, resources.getString(country.getCountryStringResId()));
		}
		Collections.sort(countries, new Comparator<Constants.COUNTRY>() {
			@Override
			public int compare(Constants.COUNTRY lhs, Constants.COUNTRY rhs) {
				return map.get(lhs).compareTo(map.get(rhs));
			}
		});
		// add countries
		clear();
		this.countries = countries;
		if (userCountry != null) {
			moveCountryToFirst(userCountry);
			setCurrentCountry(userCountry);
		}
		addAll(this.countries);
		notifyDataSetChanged();
	}

	private void moveCountryToFirst(Constants.COUNTRY country) {
		if (countries.contains(country) && countries.get(0) != country) {
			countries.remove(country);
			countries.add(0, country);
		}
	}

	public void setCurrentCountry(Constants.COUNTRY currentCountry) {
		this.currentCountry = currentCountry;
	}

	private class ProxyFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence searchingText) {
			FilterResults results = new FilterResults();

			if (searchingText == null || searchingText.length() == 0) {
				ArrayList<Constants.COUNTRY> list;
				list = new ArrayList<>(countries);
				results.values = list;
				results.count = list.size();
			} else {
				String searchString = searchingText.toString().toLowerCase();
				CountriesArrayAdapter.this.searchString = searchString;

				ArrayList<Constants.COUNTRY> values;
				values = new ArrayList<>(countries);

				ArrayList<Constants.COUNTRY> newValues = new ArrayList<>();

				for (Constants.COUNTRY value : values) {
					String valueText = getContext().getString(value.getCountryStringResId())
												   .toLowerCase();
					String countryCode = value.toString().toLowerCase();

					// First match against the whole, non-splitted value
					if (valueText.contains(searchString) || countryCode.contains(searchString)) {
						newValues.add(value);
					} else {
						String[] words = valueText.split(" ");

						// Start at index 0, in case valueText starts with space(s)
						for (String word : words) {
							if (word.contains(searchString)) {
								newValues.add(value);
								break;
							}
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			CountriesArrayAdapter.this.clear();
			//noinspection unchecked
			CountriesArrayAdapter.this.addAll((ArrayList<Constants.COUNTRY>) results.values);
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

		@Override
		public CharSequence convertResultToString(Object resultValue) {
			return resultValue == null ? ""
									   : getContext().getString(((Constants.COUNTRY) resultValue)
																  .getCountryStringResId());
		}
	}
}
