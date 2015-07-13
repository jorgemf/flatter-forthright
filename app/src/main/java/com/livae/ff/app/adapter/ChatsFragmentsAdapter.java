package com.livae.ff.app.adapter;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.fragment.ChatsPrivateFragment;
import com.livae.ff.app.fragment.ChatsPublicFragment;
import com.livae.ff.common.Constants;

public class ChatsFragmentsAdapter extends FragmentPagerAdapter {

	public static final int CHAT_FLATTERED = 0;

	public static final int CHAT_PRIVATE = 1;

	public static final int CHAT_FORTHRIGHT = 2;

	private SparseArray<Fragment> registeredFragments;

	private String[] titles;

	public ChatsFragmentsAdapter(FragmentManager fm, Resources res) {
		super(fm);
		titles = new String[3];
		titles[CHAT_FLATTERED] = res.getString(R.string.tab_flatter);
		titles[CHAT_FORTHRIGHT] = res.getString(R.string.tab_forthright);
		titles[CHAT_PRIVATE] = res.getString(R.string.tab_private);
		registeredFragments = new SparseArray<>();
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
			case CHAT_FLATTERED:
				ChatsPublicFragment flatterer = new ChatsPublicFragment();
				flatterer.setChatType(Constants.ChatType.FLATTER);
				return flatterer;
			case CHAT_FORTHRIGHT:
				ChatsPublicFragment forthright = new ChatsPublicFragment();
				forthright.setChatType(Constants.ChatType.FORTHRIGHT);
				return forthright;
			case CHAT_PRIVATE:
				return new ChatsPrivateFragment();
		}
		return null;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment fragment = (Fragment) super.instantiateItem(container, position);
		registeredFragments.put(position, fragment);
		return fragment;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		registeredFragments.remove(position);
		super.destroyItem(container, position, object);
	}

	@Override
	public int getCount() {
		return 3;
	}

	public CharSequence getPageTitle(int position) {
		return titles[position];
	}

	public Fragment getRegisteredFragment(int position) {
		return registeredFragments.get(position);
	}

}
