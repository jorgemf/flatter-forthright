package com.livae.ff.app.viewholders;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.view.CircularImageView;

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

	private Long userPhone;

	private TextView userNameTextView;

	private CircularImageView userImageView;

	private UserClickListener userClickListener;

	public UserViewHolder(View itemView, UserClickListener userClickListener) {
		super(itemView);
		this.userClickListener = userClickListener;
		itemView.setOnClickListener(this);
		userNameTextView = (TextView) itemView.findViewById(R.id.user_name);
		userImageView = (CircularImageView) itemView.findViewById(R.id.user_image);
	}

	public void clear() {
		userImageView.setImageBitmap(null);
		userNameTextView.setText(null);
		userPhone = null;
	}

	@Override
	public void onClick(View v) {
		userClickListener.userClicked(userPhone, userNameTextView, userImageView);
	}

	public Long getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(Long userPhone) {
		this.userPhone = userPhone;
	}

	public void setUserName(CharSequence name) {
		this.userNameTextView.setText(name);
	}

	public void setUserImageView(Drawable drawable) {
		this.userImageView.setImageDrawable(drawable);
	}
}
