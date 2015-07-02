package com.livae.ff.app.listener;

import android.view.View;
import android.widget.TextView;

import com.livae.ff.common.Constants;

public interface ConversationClickListener {

	public void conversationClicked(Long conversationId, String roomName, String userDisplayName,
									Long userId, Constants.ChatType chatType, Long lastMessageDate,
									TextView name, View image, String imageUri);

}
