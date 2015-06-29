package com.livae.ff.app.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.ConversationClickListener;
import com.livae.ff.app.utils.ImageUtils;
import com.livae.ff.app.utils.UnitUtils;
import com.livae.ff.app.view.AnonymousImage;
import com.livae.ff.common.Constants;

public class ConversationViewHolder extends RecyclerView.ViewHolder
  implements View.OnClickListener {

	private Long conversationId;

	private String conversationTitle;

	private Constants.ChatType conversationType;

	private String contactName;

	private String imageUri;

	private TextView userNameTextView;

	private ImageView userImageView;

	private TextView titleTextView;

	private TextView subtitleTextView;

	private TextView dateTextView;

	private AnonymousImage anonymousImageView;

	private ConversationClickListener conversationClickListener;

	private View anonymousMark;

	private View secretChatMark;

	public ConversationViewHolder(View itemView,
								  ConversationClickListener conversationClickListener) {
		super(itemView);
		this.conversationClickListener = conversationClickListener;
		itemView.setOnClickListener(this);
		userNameTextView = (TextView) itemView.findViewById(R.id.user_name);
		titleTextView = (TextView) itemView.findViewById(R.id.title);
		subtitleTextView = (TextView) itemView.findViewById(R.id.subtitle);
		dateTextView = (TextView) itemView.findViewById(R.id.date);
		userImageView = (ImageView) itemView.findViewById(R.id.image);
		anonymousImageView = (AnonymousImage) itemView.findViewById(R.id.image_anonymous);
		anonymousMark = itemView.findViewById(R.id.anonymous_mark);
		secretChatMark = itemView.findViewById(R.id.secret_mark);
	}

	public void clear() {
		userImageView.setImageBitmap(null);
		anonymousImageView.setVisibility(View.GONE);
		anonymousMark.setVisibility(View.GONE);
		secretChatMark.setVisibility(View.GONE);
		userNameTextView.setText(null);
		titleTextView.setText(null);
		subtitleTextView.setText(null);
		dateTextView.setText(null);
		userImageView.setImageResource(R.drawable.anom_user);
		conversationTitle = null;
		conversationId = null;
		conversationType = null;
		contactName = null;
		imageUri = null;
	}

	@Override
	public void onClick(View v) {
		if (conversationType == Constants.ChatType.PRIVATE_ANONYMOUS && contactName == null) {
			conversationClickListener.conversationClicked(conversationId, conversationTitle,
														  conversationType, titleTextView,
														  anonymousImageView, null);
		} else {
			conversationClickListener.conversationClicked(conversationId, contactName,
														  conversationType, userNameTextView,
														  userImageView, imageUri);
		}
	}

	public void setUserImage(String imageUri) {
		ImageUtils.loadUserImage(userImageView, imageUri);
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}

	public void setConversationType(Constants.ChatType conversationType) {
		this.conversationType = conversationType;
	}

	public void setConversationTitle(String conversationTitle) {
		this.conversationTitle = conversationTitle;
		titleTextView.setText(conversationTitle);
	}

	public void setAnonymous(boolean anonymous) {
		anonymousMark.setVisibility(anonymous ? View.VISIBLE : View.GONE);
	}

	public void setSecret(boolean secret) {
		secretChatMark.setVisibility(secret ? View.VISIBLE : View.GONE);
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
		userNameTextView.setVisibility(contactName != null ? View.VISIBLE : View.GONE);
		userNameTextView.setText(contactName);
	}

	public void setImageAnonymous(String imageAnonymous) {
		anonymousImageView.setVisibility(imageAnonymous != null ? View.VISIBLE : View.GONE);
		anonymousImageView.setSeed(imageAnonymous);
	}

	public void setLastMessage(String message, long date) {
		subtitleTextView.setText(message);
		dateTextView.setText(UnitUtils.getAgoTime(subtitleTextView.getContext(), date));
	}
}
