package com.livae.ff.app.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.livae.ff.app.Application;
import com.livae.ff.app.async.AsyncCache;
import com.livae.ff.app.listener.LifeCycle;
import com.squareup.leakcanary.RefWatcher;

public class AbstractFragment extends Fragment implements LifeCycle {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			AsyncCache.instance().onCreate(this);
		} else {
			AsyncCache.instance().onRecreate(this);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		AsyncCache.instance().onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		AsyncCache.instance().onPause(this);
	}

	@Override
	public void onDestroy() {
		AsyncCache.instance().onDestroy(this);
		super.onDestroy();
		RefWatcher refWatcher = Application.getRefWatcher(getActivity());
		refWatcher.watch(this);
	}

}
