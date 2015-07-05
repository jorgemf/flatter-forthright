package com.livae.ff.app.viewholders;

import android.view.View;

import com.livae.ff.app.listener.ChatPublicClickListener;
import com.livae.ff.app.model.ChatPublicModel;

public class ChatPublicViewHolder extends UserViewHolder {

	private ChatPublicModel chatPublicModel;

	private ChatPublicClickListener chatPublicClickListener;

	public ChatPublicViewHolder(View itemView, ChatPublicClickListener chatPublicClickListener) {
		this(itemView, new ChatPublicModel());
		this.chatPublicClickListener = chatPublicClickListener;
		itemView.setOnClickListener(this);
	}

	protected ChatPublicViewHolder(View itemView, ChatPublicModel chatPublicModel) {
		super(itemView, chatPublicModel);
		this.chatPublicModel = chatPublicModel;
	}

	@Override
	public void onClick(View v) {
		if (chatPublicClickListener != null) {
			chatPublicClickListener.chatClicked(chatPublicModel);
		}
	}

	public void setConversationId(Long conversationId) {
		chatPublicModel.conversationId = conversationId;
	}

	public void setRoomName(String roomName) {
		chatPublicModel.roomName = roomName;
	}

}
