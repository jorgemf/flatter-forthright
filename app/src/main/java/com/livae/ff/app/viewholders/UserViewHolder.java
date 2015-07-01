package com.livae.ff.app.viewholders;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.utils.ImageUtils;
import com.livae.ff.app.utils.PhoneUtils;
import com.livae.ff.app.utils.TextUtils;

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
		userImageView.setImageResource(R.drawable.ic_account_circle_white_48dp);
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

	public void setUserName(CharSequence name, String boldText) {
		if (boldText != null) {
			this.userNameTextView.setText(TextUtils.setBoldText(name, boldText, true));
		} else {
			this.userNameTextView.setText(name);
		}
	}

	public void setUserNameRes(@StringRes int stringRes) {
		this.userNameTextView.setText(stringRes);
	}

	public void setUserName(CharSequence name) {
		this.userNameTextView.setText(name);
	}

	public void setUserImageRes(@DrawableRes int drawableRed) {
		this.userImageView.setImageResource(drawableRed);
	}

	public void setUserImage(String imageUri) {
		if (imageUri != null) {
			ImageUtils.loadUserImage(userImageView, imageUri);
		} else {
			userImageView.setImageResource(R.drawable.ic_account_circle_white_48dp);
		}
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}
}
