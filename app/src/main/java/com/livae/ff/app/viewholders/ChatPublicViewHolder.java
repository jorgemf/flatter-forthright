package com.livae.ff.app.viewholders;

import android.view.View;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.ChatPublicClickListener;
import com.livae.ff.app.model.ChatPublicModel;

public class ChatPublicViewHolder extends UserViewHolder {

	private ChatPublicModel chatPublicModel;

	private TextView unreadCount;

	private ChatPublicClickListener chatPublicClickListener;

	public ChatPublicViewHolder(View itemView, ChatPublicClickListener chatPublicClickListener) {
		this(itemView, new ChatPublicModel());
		this.chatPublicClickListener = chatPublicClickListener;
		itemView.setOnClickListener(this);
	}

	protected ChatPublicViewHolder(View itemView, ChatPublicModel chatPublicModel) {
		super(itemView, chatPublicModel);
		this.chatPublicModel = chatPublicModel;
		unreadCount = (TextView) itemView.findViewById(R.id.unread_count);
	}

	@Override
	public void clear() {
		super.clear();
		if (unreadCount != null) {
			unreadCount.setText(null);
			unreadCount.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		if (chatPublicClickListener != null) {
			chatPublicClickListener.chatClicked(chatPublicModel);
		}
	}

	public void setUnreadCount(int count) {
		if (unreadCount != null) {
			if (count > 0) {
				unreadCount.setVisibility(View.VISIBLE);
				if (count > 99) {
					unreadCount.setText("+99");
				} else {
					unreadCount.setText(Integer.toString(count));
				}
			} else {
				unreadCount.setText(null);
				unreadCount.setVisibility(View.GONE);
			}
		}
	}

	public void setConversationId(Long conversationId) {
		chatPublicModel.conversationId = conversationId;
	}

	public void setRoomName(String roomName) {
		chatPublicModel.roomName = roomName;
	}

	public ChatPublicModel getModel() {
		return chatPublicModel;
	}
}
