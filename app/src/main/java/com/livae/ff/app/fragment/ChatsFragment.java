package com.livae.ff.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.adapter.ConversationsAdapter;
import com.livae.ff.app.listener.ConversationClickListener;
import com.livae.ff.app.receiver.NotificationDisabledReceiver;
import com.livae.ff.common.Constants;
import com.livae.ff.common.model.Notification;

public class ChatsFragment extends AbstractFragment
  implements ConversationClickListener, NotificationDisabledReceiver.CloudMessagesDisabledListener {

	private ConversationsAdapter conversationsAdapter;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_private_chats, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		conversationsAdapter = new ConversationsAdapter(getActivity(), this);
		recyclerView.setAdapter(conversationsAdapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
	}

	@Override
	public void conversationClicked(Long conversationId, String roomName,
									Constants.ChatType chatType, TextView name, View image,
									String imageUri) {
		// TODO
	}

	@Override
	public boolean onNotificationReceived(Notification notification) {
		// TODO
		return true;
	}

	public int getUnreadChats() {
		// TODO
		return 1;
	}
}
