package com.livae.ff.app.viewholders;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.utils.ImageUtils;
import com.livae.ff.app.utils.PhoneUtils;

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

	private Long userPhone;

	private Long conversationId;

	private TextView userNameTextView;

	private TextView userPhoneTextView;

	private ImageView userImageView;

	private UserClickListener userClickListener;

	public UserViewHolder(View itemView, UserClickListener userClickListener) {
		super(itemView);
		this.userClickListener = userClickListener;
		itemView.setOnClickListener(this);
		userNameTextView = (TextView) itemView.findViewById(R.id.user_name);
		userPhoneTextView = (TextView) itemView.findViewById(R.id.user_phone);
		userImageView = (ImageView) itemView.findViewById(R.id.user_image);
	}

	public void clear() {
		userImageView.setImageBitmap(null);
		userNameTextView.setText(null);
		userPhoneTextView.setText(null);
		userImageView.setImageResource(R.drawable.anom_user);
		userPhone = null;
		conversationId = null;
	}

	@Override
	public void onClick(View v) {
		userClickListener.userClicked(userPhone, conversationId, userNameTextView, userImageView);
	}

	public Long getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(Long userPhone, String countryISO) {
		this.userPhone = userPhone;
		this.userPhoneTextView.setText(PhoneUtils.getPrettyPrint(userPhone, countryISO));
	}

	public void setUserName(CharSequence name) {
		this.userNameTextView.setText(name);
	}

	public void setUser(CharSequence name) {
		this.userNameTextView.setText(name);
	}

	public void setUserImageView(Drawable drawable) {
		this.userImageView.setImageDrawable(drawable);
	}

	public void setUserImage(String imageUri) {
		ImageUtils.loadUserImage(userImageView, imageUri);
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}
}
