package com.livae.ff.app.model;

import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.R;

public class UserModel {

	public Long userId;

	public String userDisplayName;

	public String userImageUri;

	public TextView displayNameTextView;

	public TextView phoneTextView;

	public ImageView userImageView;

	public Boolean userBlocked;

	public Long rawContactId;

	public void clear() {
		userId = null;
		userDisplayName = null;
		userImageUri = null;
		userBlocked = null;
		rawContactId = null;
		if (displayNameTextView != null) {
			displayNameTextView.setText(null);
		}
		if (phoneTextView != null) {
			phoneTextView.setText(null);
		}
		if (userImageView != null) {
			userImageView.setImageResource(R.drawable.ic_account_circle_white_48dp);
		}
	}
}
