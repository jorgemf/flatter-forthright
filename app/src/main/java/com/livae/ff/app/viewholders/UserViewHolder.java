package com.livae.ff.app.viewholders;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.livae.ff.app.listener.UserClickListener;

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

	private Long userPhone;

	private TextView userNameTextView;

	private TextView userPhoneTextView;

	private ImageView favoriteImageView;

	private ImageView userImageView;

	private UserClickListener userClickListener;

	public UserViewHolder(View itemView, UserClickListener userClickListener) {
		super(itemView);
		this.userClickListener = userClickListener;
		itemView.setOnClickListener(this);
		userNameTextView = (TextView) itemView.findViewById(R.id.user_name);
		userPhoneTextView = (TextView) itemView.findViewById(R.id.user_phone);
		userImageView = (ImageView) itemView.findViewById(R.id.user_image);
		favoriteImageView = (ImageView) itemView.findViewById(R.id.user_favorite);
	}

	public void clear() {
		userImageView.setImageBitmap(null);
		userNameTextView.setText(null);
		userPhoneTextView.setText(null);
		userPhone = null;
		// TODO user favorite image view
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
		this.userPhoneTextView.setText(userPhone);// TODO parse text
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

	public void setFavorite(boolean favorite) {
		// TODO
		if (favorite) {

		} else {

		}
	}
}
