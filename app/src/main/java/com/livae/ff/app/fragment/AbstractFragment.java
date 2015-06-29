package com.livae.ff.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.adapter.EndlessCursorAdapter;
import com.livae.ff.common.Constants.ChatType;

public class AbstractFragment extends Fragment implements EndlessCursorAdapter.ViewCreator {


	public View createLoadingView(LayoutInflater layoutInflater, ViewGroup parent) {
		return layoutInflater.inflate(R.layout.item_loading, parent, false);
	}

	public View createErrorView(LayoutInflater layoutInflater, ViewGroup parent) {
		return layoutInflater.inflate(R.layout.item_retry, parent, false);
	}
}
