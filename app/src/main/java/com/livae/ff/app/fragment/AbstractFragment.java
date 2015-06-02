package com.livae.ff.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.adapter.EndlessCursorAdapter;
import com.livae.ff.common.Constants.CommentType;

public class AbstractFragment extends Fragment implements EndlessCursorAdapter.ViewCreator {

	private static final String SAVE_COMMENT_TYPE = "SAVE_COMMENT_TYPE";

	private CommentType commentType;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(SAVE_COMMENT_TYPE, commentType);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			commentType = (CommentType) savedInstanceState.getSerializable(SAVE_COMMENT_TYPE);
		}
	}

	public CommentType getCommentType() {
		return commentType;
	}

	public void setCommentType(CommentType commentType) {
		this.commentType = commentType;
	}

	public View createLoadingView(LayoutInflater layoutInflater, ViewGroup parent) {
		return layoutInflater.inflate(R.layout.item_loading, parent, false);
	}

	public View createErrorView(LayoutInflater layoutInflater, ViewGroup parent) {
		return layoutInflater.inflate(R.layout.item_retry, parent, false);
	}
}
