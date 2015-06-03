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

	private static final String SAVE_COMMENT_TYPE = "SAVE_COMMENT_TYPE";

	private ChatType chatType;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(SAVE_COMMENT_TYPE, chatType);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			chatType = (ChatType) savedInstanceState.getSerializable(SAVE_COMMENT_TYPE);
		}
	}

	public ChatType getChatType() {
		return chatType;
	}

	public void setChatType(ChatType chatType) {
		this.chatType = chatType;
	}

	public View createLoadingView(LayoutInflater layoutInflater, ViewGroup parent) {
		return layoutInflater.inflate(R.layout.item_loading, parent, false);
	}

	public View createErrorView(LayoutInflater layoutInflater, ViewGroup parent) {
		return layoutInflater.inflate(R.layout.item_retry, parent, false);
	}
}
