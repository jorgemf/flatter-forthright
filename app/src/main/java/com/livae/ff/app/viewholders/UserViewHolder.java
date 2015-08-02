package com.livae.ff.app.viewholders;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.model.UserModel;
import com.livae.ff.app.utils.ImageUtils;
import com.livae.ff.app.utils.PhoneUtils;
import com.livae.ff.app.utils.TextUtils;

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

	private UserModel userModel;

	private UserClickListener userClickListener;

	public UserViewHolder(View itemView, UserClickListener userClickListener) {
		this(itemView, new UserModel());
		this.userClickListener = userClickListener;
		itemView.setOnClickListener(this);
	}

	protected UserViewHolder(View itemView, UserModel userModel) {
		super(itemView);
		this.userModel = userModel;
		userModel.displayNameTextView = (TextView) itemView.findViewById(R.id.user_name);
		userModel.phoneTextView = (TextView) itemView.findViewById(R.id.subtitle);
		userModel.userImageView = (ImageView) itemView.findViewById(R.id.user_image);
	}

	public void clear() {
		userModel.clear();
	}

	@Override
	public void onClick(View v) {
		if (userClickListener != null) {
			userClickListener.userClicked(userModel);
		}
	}

	public Long getUserId() {
		return userModel.userId;
	}

	public void setUserPhone(Long userPhone) {
		userModel.userId = userPhone;
	}

	public void setUserPhone(Long userPhone, String countryISO) {
		userModel.userId = userPhone;
		if (userModel.phoneTextView != null) {
			String phoneString = PhoneUtils.getPrettyPrint(userPhone, countryISO);
			userModel.phoneTextView.setText(phoneString);
		}
	}

	public void setUserName(String name, String boldText) {
		userModel.userDisplayName = name;
		if (userModel.displayNameTextView != null) {
			if (boldText != null) {
				userModel.displayNameTextView.setText(TextUtils.setBoldText(name, boldText, true));
			} else {
				userModel.displayNameTextView.setText(name);
			}
		}
	}

	public void setUserNameRes(@StringRes int stringRes) {
		if (userModel.displayNameTextView != null) {
			userModel.displayNameTextView.setText(stringRes);
		}
	}

	public void setUserName(String name) {
		userModel.userDisplayName = name;
		if (userModel.displayNameTextView != null) {
			userModel.displayNameTextView.setText(name);
		}
	}

	public void setUserImageRes(@DrawableRes int drawableRed) {
		if (userModel.userImageView != null) {
			userModel.userImageView.setImageResource(drawableRed);
		}
	}

	public void setUserImage(String imageUri) {
		userModel.userImageUri = imageUri;
		if (userModel.userImageView != null) {
			if (imageUri != null) {
				ImageUtils.loadUserImage(userModel.userImageView, imageUri);
			} else {
				userModel.userImageView.setImageResource(R.drawable.ic_account_circle_white_48dp);
			}
		}
	}

	public void setUserBlocked(boolean userBlocked) {
		userModel.userBlocked = userBlocked;
	}

	public void setRawContactId(Long rawContactId) {
		userModel.rawContactId = rawContactId;
	}
}
