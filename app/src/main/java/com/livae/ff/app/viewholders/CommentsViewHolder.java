package com.livae.ff.app.viewholders;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.R;
import com.livae.apphunt.app.listener.ApplicationClickListener;
import com.livae.apphunt.app.listener.CommentActionListener;
import com.livae.apphunt.app.listener.UserClickListener;
import com.livae.apphunt.app.utils.EnumUtils;
import com.livae.apphunt.app.utils.ImageUtils;
import com.livae.apphunt.app.utils.UnitUtils;
import com.livae.apphunt.common.Constants.CommentVoteType;
import com.livae.apphunt.common.Constants.Relationship;

public class CommentsViewHolder extends RecyclerView.ViewHolder
  implements View.OnClickListener, View.OnLongClickListener {

	private Long commentId;

	private Long userId;

	private Long appId;

	private SimpleDraweeView userImage;

	private SimpleDraweeView appImage;

	private TextView userRelationship;

	private TextView title;

	private TextView subtitle;

	private TextView comment;

	private TextView date;

	private CommentActionListener commentActionListener;

	private UserClickListener userClickListener;

	private ApplicationClickListener applicationClickListener;

	private Button voteUpButton;

	private Button voteDownButton;

	private CommentVoteType voteType;

	public CommentsViewHolder(View itemView, CommentActionListener commentActionListener,
							  UserClickListener userClickListener) {
		this(itemView, commentActionListener, userClickListener, null);
	}

	public CommentsViewHolder(View itemView, CommentActionListener commentActionListener,
							  ApplicationClickListener applicationClickListener) {
		this(itemView, commentActionListener, null, applicationClickListener);
	}

	private CommentsViewHolder(View itemView, CommentActionListener commentActionListener,
							   UserClickListener userClickListener,
							   ApplicationClickListener applicationClickListener) {
		super(itemView);
		this.commentActionListener = commentActionListener;
		this.userClickListener = userClickListener;
		this.applicationClickListener = applicationClickListener;
		userImage = (SimpleDraweeView) itemView.findViewById(R.id.user_image);
		appImage = (SimpleDraweeView) itemView.findViewById(R.id.app_image);
		userRelationship = (TextView) itemView.findViewById(R.id.relationship);
		userRelationship.setOnClickListener(this);
		title = (TextView) itemView.findViewById(R.id.title);
		subtitle = (TextView) itemView.findViewById(R.id.subtitle);
		comment = (TextView) itemView.findViewById(R.id.comment);
		date = (TextView) itemView.findViewById(R.id.comment_date);
		voteUpButton = (Button) itemView.findViewById(R.id.vote_up);
		voteDownButton = (Button) itemView.findViewById(R.id.vote_down);
		voteUpButton.setOnClickListener(this);
		voteDownButton.setOnClickListener(this);
		itemView.findViewById(R.id.comment_header).setOnClickListener(this);
		if (Application.getSeeAdmin()) {
			Button buttonDelete = (Button) itemView.findViewById(R.id.button_delete);
			buttonDelete.setOnClickListener(this);
			buttonDelete.setVisibility(View.VISIBLE);
		}
	}

	public void clear() {
		userImage.setImageURI(null);
		appImage.setImageURI(null);
		userRelationship.setText(null);
		userRelationship.setVisibility(View.GONE);
		title.setText(null);
		subtitle.setText(null);
		comment.setText(null);
		date.setText(null);
		comment.setOnLongClickListener(null);
	}

	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public void setUserImageUrl(String userImageUrl) {
		ImageUtils.loadDefault(this.userImage, userImageUrl);
		this.appImage.setVisibility(View.INVISIBLE);
		this.userImage.setVisibility(View.VISIBLE);
	}

	public void setAppImageUrl(String appImageUrl) {
		ImageUtils.loadDefault(this.appImage, appImageUrl);
		this.appImage.setVisibility(View.VISIBLE);
		this.userImage.setVisibility(View.INVISIBLE);
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

	public void setTitle(String title) {
		Typeface nameTypeFace = this.title.getTypeface();
		this.title.setTypeface(Typeface.create(nameTypeFace, Typeface.NORMAL));
		this.title.setText(title);
	}

	public void setSubtitle(String subtitle) {
		this.subtitle.setText(subtitle);
	}

	public void setComment(String comment) {
		this.comment.setText(comment);
	}

	public void setDate(long date) {
		this.date.setText(UnitUtils.getAgoTime(this.date.getContext(), date));
	}

	public void setVotes(int upVotes, int downVotes) {
		voteUpButton.setText(Integer.toString(upVotes));
		voteDownButton.setText(Integer.toString(downVotes));
		// TODO make text lighter if the comment is bad, highlight it if it is good
	}

	public void setCanVote(boolean canVote) {
		voteUpButton.setEnabled(canVote);
		voteDownButton.setEnabled(canVote);
	}

	public void canBeUpdated(boolean canBeUpdated) {
		if (canBeUpdated) {
			comment.setOnLongClickListener(this);
		} else {
			comment.setOnLongClickListener(null);
		}
	}

	public void setUserAnonymous() {
		Typeface nameTypeFace = title.getTypeface();
		title.setTypeface(Typeface.create(nameTypeFace, Typeface.ITALIC));
		title.setText(R.string.anonymous);
		this.userImage.setImageResource(R.drawable.anom_user);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.vote_up:
				if (voteType == CommentVoteType.UP) {
					commentActionListener.commentNoVoted(commentId, userId, getAdapterPosition());
				} else {
					commentActionListener.commentVotedUp(commentId, userId, getAdapterPosition());
				}
				break;
			case R.id.vote_down:
				if (voteType == CommentVoteType.DOWN) {
					commentActionListener.commentNoVoted(commentId, userId, getAdapterPosition());
				} else {
					commentActionListener.commentVotedDown(commentId, userId, getAdapterPosition());
				}
				break;
			case R.id.button_delete:
				if (Application.getSeeAdmin()) {
					commentActionListener.commentDelete(commentId, comment.getText().toString(),
														getAdapterPosition());
				}
				break;
			case R.id.comment_header:
				if (userClickListener != null) {
					userClickListener.userClicked(userId, userImage, title, subtitle, null);
				}
				if (applicationClickListener != null) {
					applicationClickListener.applicationClicked(appId, null, appImage, title,
																subtitle, null);
				}
				break;
			case R.id.relationship:
				userClickListener.userRelationshipClicked();
				break;
		}
	}

	public void setVoteType(CommentVoteType voteType) {
		this.voteType = voteType;
		Resources resources = itemView.getContext().getResources();
		int colorAccent = resources.getColor(R.color.accent);
		int colorNormal = resources.getColor(R.color.black);
		Drawable thumbUp = resources.getDrawable(R.drawable.ic_action_thumb_up);
		Drawable thumbDown = resources.getDrawable(R.drawable.ic_action_thumb_down);
		Drawable thumbUpAccent = resources.getDrawable(R.drawable.ic_action_thumb_up_accent);
		Drawable thumbDownAccent = resources.getDrawable(R.drawable.ic_action_thumb_down_accent);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			if (voteType == null) {
				voteUpButton.setTextColor(colorNormal);
				voteUpButton.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUp, null, null,
																			 null);
				voteDownButton.setTextColor(colorNormal);
				voteDownButton.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDown, null,
																			   null, null);
			} else {
				switch (voteType) {
					case UP:
						voteUpButton.setTextColor(colorAccent);
						voteUpButton.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUpAccent,
																					 null, null,
																					 null);
						voteDownButton.setTextColor(colorNormal);
						voteDownButton.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDown,
																					   null, null,
																					   null);
						break;
					case DOWN:
						voteUpButton.setTextColor(colorNormal);
						voteUpButton.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUp, null,
																					 null, null);
						voteDownButton.setTextColor(colorAccent);
						voteDownButton
						  .setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDownAccent, null,
																		   null, null);
						break;
				}
			}
		} else {
			if (voteType == null) {
				voteUpButton.setTextColor(colorNormal);
				voteUpButton.setCompoundDrawablesWithIntrinsicBounds(thumbUp, null, null, null);
				voteDownButton.setTextColor(colorNormal);
				voteDownButton.setCompoundDrawablesWithIntrinsicBounds(thumbDown, null, null, null);
			} else {
				switch (voteType) {
					case UP:
						voteUpButton.setTextColor(colorAccent);
						voteUpButton.setCompoundDrawablesWithIntrinsicBounds(thumbUpAccent, null,
																			 null, null);
						voteDownButton.setTextColor(colorNormal);
						voteDownButton.setCompoundDrawablesWithIntrinsicBounds(thumbDown, null,
																			   null, null);
						break;
					case DOWN:
						voteUpButton.setTextColor(colorNormal);
						voteUpButton.setCompoundDrawablesWithIntrinsicBounds(thumbUp, null, null,
																			 null);
						voteDownButton.setTextColor(colorAccent);
						voteDownButton.setCompoundDrawablesWithIntrinsicBounds(thumbDownAccent,
																			   null, null, null);
						break;
				}
			}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		commentActionListener.commentUpdate(commentId, comment.getText().toString(),
											getAdapterPosition());
		return true;
	}
}
