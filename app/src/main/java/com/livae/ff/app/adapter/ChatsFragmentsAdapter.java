package com.livae.ff.app.adapter;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.livae.ff.app.R;
import com.livae.ff.app.fragment.ChatsFragment;
import com.livae.ff.app.fragment.PublicChatsFragment;
import com.livae.ff.common.Constants;

public class ChatsFragmentsAdapter extends FragmentPagerAdapter {

	private String[] titles;

	public ChatsFragmentsAdapter(FragmentManager fm, Resources res) {
		super(fm);
		titles = new String[3];
		titles[0] = res.getString(R.string.tab_flatter);
		titles[1] = res.getString(R.string.tab_forthright);
		titles[2] = res.getString(R.string.tab_private);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case 0:
				PublicChatsFragment flatterer= new PublicChatsFragment();
				flatterer.setChatType(Constants.ChatType.FLATTER);
				return flatterer;
			case 1:
				PublicChatsFragment forthright= new PublicChatsFragment();
				forthright.setChatType(Constants.ChatType.FORTHRIGHT);
				return forthright;
			case 2:
				return new ChatsFragment();
		}
		return null;
	}

	@Override
	public int getCount() {
		return 3;
	}

	public CharSequence getPageTitle(int position) {
		return titles[position];
	}
}
