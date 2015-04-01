package com.livae.ff.app.viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.R;
import com.livae.apphunt.app.listener.UserClickListener;
import com.livae.apphunt.app.utils.EnumUtils;
import com.livae.apphunt.app.utils.ImageUtils;
import com.livae.apphunt.common.Constants.Relationship;

public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

	private Long userId;

	private SimpleDraweeView userImage;

	private TextView userRelationship;

	private TextView userName;

	private TextView userTagLine;

	private TextView timesFlaggedText;

	private UserClickListener userClickListener;

	private View cardView;

	public UserViewHolder(View itemView, UserClickListener userClickListener) {
		super(itemView);
		this.userClickListener = userClickListener;
		userImage = (SimpleDraweeView) itemView.findViewById(R.id.user_image);
		userRelationship = (TextView) itemView.findViewById(R.id.relationship);
		userRelationship.setOnClickListener(this);
		userName = (TextView) itemView.findViewById(R.id.user_name);
		userTagLine = (TextView) itemView.findViewById(R.id.user_tagline);
		timesFlaggedText = (TextView) itemView.findViewById(R.id.user_flagged_times);
		itemView.findViewById(R.id.clickable_container).setOnClickListener(this);
		cardView = itemView.findViewById(R.id.card_view);
	}

	public void clear() {
		userImage.setImageURI(null);
		userRelationship.setText(null);
		userName.setText(null);
		userTagLine.setText(null);
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public void setUserImageUrl(String userImageUrl) {
		if (userImageUrl == null) {
			this.userImage.setImageResource(R.drawable.anom_user);
		} else {
			ImageUtils.loadDefault(this.userImage, userImageUrl);
		}
	}

	public void setUserRelationship(Relationship userRelationship) {
		if (userRelationship != null) {
			this.userRelationship.setVisibility(View.VISIBLE);
			Context context = this.userRelationship.getContext();
			CharSequence c = EnumUtils.getRelationshipChar(context, userRelationship);
			this.userRelationship.setText(c);
		} else {
			this.userRelationship.setVisibility(View.GONE);
		}
	}

	public void setUserName(String userName) {
		this.userName.setText(userName);
		Typeface nameTypeFace = this.userName.getTypeface();
		this.userName.setTypeface(Typeface.create(nameTypeFace, Typeface.NORMAL));
		this.userName.setText(userName);
		if (userName == null || userName.trim().length() == 0) {
			this.userName.setTypeface(Typeface.create(nameTypeFace, Typeface.ITALIC));
			this.userName.setText(R.string.anonymous);
		}
	}

	public void setUserTagLine(String userTagLine) {
		this.userTagLine.setText(userTagLine);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.relationship:
				userClickListener.userRelationshipClicked();
				break;
			case R.id.clickable_container:
				userClickListener.userClicked(userId, userImage, userName, userTagLine, cardView);
		}
	}

	public void setTimesFlagged(Integer timesFlagged) {
		timesFlaggedText.setText(null);
		if (timesFlagged != null && timesFlagged > 0 && Application.getSeeAdmin()) {
			timesFlaggedText.setText(Integer.toString(timesFlagged));
			timesFlaggedText.setVisibility(View.VISIBLE);
		}
	}
}
