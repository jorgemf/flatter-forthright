package com.livae.ff.app.model;

import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.livae.ff.app.ui.view.AnonymousImage;
import com.livae.ff.common.Constants.ChatType;

public class ChatPrivateModel extends ChatPublicModel implements ContextMenu.ContextMenuInfo {

	public String conversationSubtitle;

	public ChatType chatType;

	public AnonymousImage anonymousImageView;

	public TextView conversationTitleView;

	public TextView conversationSubtitleView;

	public Long lastAccess;

	public Long lastMessage;

	public Integer unreadMessages;

	public void clear() {
		super.clear();
		conversationSubtitle = null;
		chatType = null;
		lastAccess = null;
		lastMessage = null;
		if (anonymousImageView != null) {
			anonymousImageView.setVisibility(View.GONE);
		}
		if (conversationTitleView != null) {
			conversationTitleView.setText(null);
		}
		if (conversationSubtitleView != null) {
			conversationSubtitleView.setText(null);
		}
	}
}
