package com.livae.ff.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.adapter.EndlessCursorAdapter;
import com.livae.ff.app.listener.OnLifeCycleListener;

import java.util.ArrayList;
import java.util.List;

public class AbstractFragment extends Fragment implements EndlessCursorAdapter.ViewCreator {

	private List<OnLifeCycleListener> onLifeCycleListenerList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onLifeCycleListenerList = new ArrayList<>();
	}

	@Override
	public void onPause() {
		for (OnLifeCycleListener listener : onLifeCycleListenerList) {
			listener.onPause();
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		for (OnLifeCycleListener listener : onLifeCycleListenerList) {
			listener.onDestroy();
		}
		super.onDestroy();
	}

	public void addLifeCycleListener(OnLifeCycleListener cycleListener) {
		if (!onLifeCycleListenerList.contains(cycleListener)) {
			onLifeCycleListenerList.add(cycleListener);
		}
	}

	public void removeLifeCycleListener(OnLifeCycleListener cycleListener) {
		onLifeCycleListenerList.remove(cycleListener);
	}

	public View createLoadingView(LayoutInflater layoutInflater, ViewGroup parent) {
		return layoutInflater.inflate(R.layout.item_loading, parent, false);
	}

	public View createErrorView(LayoutInflater layoutInflater, ViewGroup parent) {
		return layoutInflater.inflate(R.layout.item_retry, parent, false);
	}

}
