package com.livae.ff.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.adapter.PublicChatsAdapter;
import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.common.Constants;

public class PublicChatsFragment extends AbstractFragment implements UserClickListener {

	private static final String SAVE_COMMENT_TYPE = "SAVE_COMMENT_TYPE";

	private Constants.ChatType chatType;

	protected PublicChatsAdapter publicChatsAdapter;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list_items, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		publicChatsAdapter = new PublicChatsAdapter(getActivity(), this);
		recyclerView.setAdapter(publicChatsAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			chatType = (Constants.ChatType) savedInstanceState.getSerializable(SAVE_COMMENT_TYPE);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(SAVE_COMMENT_TYPE, chatType);
	}

	public Constants.ChatType getChatType() {
		return chatType;
	}

	public void setChatType(Constants.ChatType chatType) {
		this.chatType = chatType;
	}

	@Override
	public void userClicked(Long userId, Long conversationId, TextView name, ImageView image) {
		// TODO
	}
}
