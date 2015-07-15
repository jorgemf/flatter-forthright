package com.livae.ff.app.viewholders;

import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.ChatPrivateClickListener;
import com.livae.ff.app.model.ChatPrivateModel;
import com.livae.ff.app.utils.ImageUtils;
import com.livae.ff.app.utils.TextUtils;
import com.livae.ff.app.utils.UnitUtils;
import com.livae.ff.app.view.AnonymousImage;
import com.livae.ff.common.Constants;

public class ChatPrivateViewHolder extends ChatPublicViewHolder {

	private ChatPrivateModel chatPrivateModel;

	private TextView dateTextView;

	private ChatPrivateClickListener chatPrivateClickListener;

	private View anonymousMark;

	private View secretChatMark;

	public ChatPrivateViewHolder(View itemView, ChatPrivateClickListener chatPrivateClickListener) {
		this(itemView, new ChatPrivateModel());
		this.chatPrivateClickListener = chatPrivateClickListener;
		itemView.setOnClickListener(this);
	}

	protected ChatPrivateViewHolder(View itemView, ChatPrivateModel chatPrivateModel) {
		super(itemView, chatPrivateModel);
		this.chatPrivateModel = chatPrivateModel;

		chatPrivateModel.displayNameTextView = (TextView) itemView.findViewById(R.id.user_name);
		chatPrivateModel.conversationTitleView = (TextView) itemView.findViewById(R.id.title);
		chatPrivateModel.conversationSubtitleView = (TextView) itemView.findViewById(R.id.subtitle);
		chatPrivateModel.anonymousImageView = (AnonymousImage) itemView
																 .findViewById(R.id.anonymous_image);
		dateTextView = (TextView) itemView.findViewById(R.id.date);
		anonymousMark = itemView.findViewById(R.id.anonymous_mark);
		secretChatMark = itemView.findViewById(R.id.secret_mark);
	}

	@Override
	public void clear() {
		super.clear();
		chatPrivateModel.clear();
		if (anonymousMark != null) {
			anonymousMark.setVisibility(View.GONE);
		}
		if (secretChatMark != null) {
			secretChatMark.setVisibility(View.GONE);
		}
		if (dateTextView != null) {
			dateTextView.setText(null);
			dateTextView.setVisibility(View.VISIBLE);
		}
		if (chatPrivateModel.conversationSubtitleView != null) {
			chatPrivateModel.conversationSubtitleView.setVisibility(View.VISIBLE);
		}
		if (chatPrivateModel.displayNameTextView != null) {
			chatPrivateModel.displayNameTextView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		if (chatPrivateClickListener != null) {
			chatPrivateClickListener.chatClicked(chatPrivateModel);
		}
	}

	public void setUserName(String name, String boldText) {
		super.setUserName(name, boldText);
		if (chatPrivateModel.displayNameTextView != null) {
			chatPrivateModel.displayNameTextView.setVisibility(name != null ? View.VISIBLE
																			: View.INVISIBLE);
		}
	}

	public void setUserImage(String imageUri) {
		if (imageUri != null) {
			ImageUtils.loadUserImage(chatPrivateModel.userImageView, imageUri);
		} else {
			chatPrivateModel.userImageView
			  .setImageResource(R.drawable.ic_account_circle_white_48dp);
		}
	}

	public void setChatType(Constants.ChatType chatType) {
		chatPrivateModel.chatType = chatType;
	}

	public void setConversationTitle(String conversationTitle, String boldText) {
		if (conversationTitle != null && chatPrivateModel.displayNameTextView != null) {
			chatPrivateModel.displayNameTextView.setVisibility(View.GONE);
		}
		chatPrivateModel.roomName = conversationTitle;
		if (boldText == null) {
			chatPrivateModel.conversationTitleView.setText(conversationTitle);
		} else {
			final SpannableString text = TextUtils.setBoldText(conversationTitle, boldText, true);
			chatPrivateModel.conversationTitleView.setText(text);
		}
	}

	public void setAnonymous(boolean anonymous) {
		anonymousMark.setVisibility(anonymous ? View.VISIBLE : View.GONE);
	}

	public void setSecret(boolean secret) {
		if (secret) {
			secretChatMark.setVisibility(View.VISIBLE);
			if (chatPrivateModel.conversationSubtitleView != null) {
				chatPrivateModel.conversationSubtitleView.setVisibility(View.GONE);
			}
			if (dateTextView != null) {
				dateTextView.setVisibility(View.GONE);
			}
		} else {
			secretChatMark.setVisibility(View.GONE);
			if (chatPrivateModel.conversationSubtitleView != null) {
				chatPrivateModel.conversationSubtitleView.setVisibility(View.VISIBLE);
			}
			if (dateTextView != null) {
				dateTextView.setVisibility(View.VISIBLE);
			}
		}
	}

	public void setImageAnonymous(Long imageAnonymous) {
		if (chatPrivateModel.anonymousImageView != null) {
			chatPrivateModel.anonymousImageView.setVisibility(imageAnonymous != null ? View.VISIBLE
																					 : View.GONE);
			chatPrivateModel.anonymousImageView.setSeed(imageAnonymous);
		}
	}

	public void setImageAnonymous(String imageAnonymous) {
		if (chatPrivateModel.anonymousImageView != null) {
			chatPrivateModel.anonymousImageView.setVisibility(imageAnonymous != null ? View.VISIBLE
																					 : View.GONE);
			chatPrivateModel.anonymousImageView.setSeed(imageAnonymous);
		}
	}

	public void setLastMessage(String message, long date) {
		chatPrivateModel.conversationSubtitle = message;
		if (chatPrivateModel.conversationSubtitleView != null) {
			chatPrivateModel.conversationSubtitleView.setText(message);
		}
		if (dateTextView != null) {
			dateTextView.setText(UnitUtils.getAgoTime(dateTextView.getContext(), date));
		}
		chatPrivateModel.lastMessage = date;
	}

	public void setLastAccessDate(long date) {
		chatPrivateModel.lastAccess = date;
	}

}
