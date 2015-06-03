package com.livae.ff.app.adapter;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.livae.ff.app.R;
import com.livae.ff.app.fragment.ContactsFragment;
import com.livae.ff.common.Constants.ChatType;

public class ChatsFragmentsAdapter extends FragmentPagerAdapter {

	private String[] titles;

	public ChatsFragmentsAdapter(FragmentManager fm, Resources res) {
		super(fm);
		final ChatType[] chatTypes = ChatType.values();
		titles = new String[chatTypes.length];
		for (ChatType chatType : chatTypes) {
			switch (chatType) {
				case FLATTER:
					titles[chatType.ordinal()] = res.getString(R.string.tab_flatter);
					break;
				case FORTHRIGHT:
					titles[chatType.ordinal()] = res.getString(R.string.tab_forthright);
					break;
				case PRIVATE:
					titles[chatType.ordinal()] = res.getString(R.string.tab_private);
					break;
			}
		}
	}

	@Override
	public Fragment getItem(int position) {
		ChatType chatType = ChatType.values()[position];
		switch (chatType) {
			case FLATTER:
				return new ContactsFragment();
			case FORTHRIGHT:
				return new ContactsFragment();
			case PRIVATE:
				return new ContactsFragment();
		}
		// TODO
		return null;
	}

	@Override
	public int getCount() {
		return ChatType.values().length;
	}

	public CharSequence getPageTitle(int position) {
		return titles[position];
	}
}
