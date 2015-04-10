package com.livae.ff.app.viewholders;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.listener.CommentActionListener;
import com.livae.ff.app.utils.ImageUtils;
import com.livae.ff.app.utils.UnitUtils;
import com.livae.ff.common.Constants.CommentVoteType;

public class CommentsViewHolder extends RecyclerView.ViewHolder
  implements View.OnClickListener, View.OnLongClickListener {

	private Long commentId;

	private Long userId;

	private ImageView userImage;

	private TextView title;

	private TextView comment;

	private TextView date;

	private CommentActionListener commentActionListener;

	private Button voteAgreeButton;

	private Button voteDisagreeButton;

	private CommentVoteType voteType;

	public CommentsViewHolder(View itemView, CommentActionListener commentActionListener) {
		super(itemView);
		this.commentActionListener = commentActionListener;
		userImage = (ImageView) itemView.findViewById(R.id.user_image);
		title = (TextView) itemView.findViewById(R.id.title);
		comment = (TextView) itemView.findViewById(R.id.comment);
		date = (TextView) itemView.findViewById(R.id.comment_date);
		voteAgreeButton = (Button) itemView.findViewById(R.id.vote_agree);
		voteDisagreeButton = (Button) itemView.findViewById(R.id.vote_disagree);
		voteAgreeButton.setOnClickListener(this);
		voteDisagreeButton.setOnClickListener(this);
		if (Application.getSeeAdmin()) {
			Button buttonDelete = (Button) itemView.findViewById(R.id.button_delete);
			buttonDelete.setOnClickListener(this);
			buttonDelete.setVisibility(View.VISIBLE);
		}
	}

	public void clear() {
		userImage.setImageBitmap(null);
		title.setText(null);
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

	public void setUserImageUrl(String userImageUrl) {
		ImageUtils.loadDefault(this.userImage, userImageUrl);
		this.userImage.setVisibility(View.VISIBLE);
	}

	public void setTitle(String title) {
		Typeface nameTypeFace = this.title.getTypeface();
		this.title.setTypeface(Typeface.create(nameTypeFace, Typeface.NORMAL));
		this.title.setText(title);
	}

	public void setComment(String comment) {
		this.comment.setText(comment);
	}

	public void setDate(long date) {
		this.date.setText(UnitUtils.getAgoTime(this.date.getContext(), date));
	}

	public void setVotes(int upVotes, int downVotes) {
		voteAgreeButton.setText(Integer.toString(upVotes));
		voteDisagreeButton.setText(Integer.toString(downVotes));
		// TODO make text lighter if the comment is bad, highlight it if it is good
	}

	public void setCanVote(boolean canVote) {
		voteAgreeButton.setEnabled(canVote);
		voteDisagreeButton.setEnabled(canVote);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.vote_agree:
				if (voteType == CommentVoteType.AGREE) {
					commentActionListener.commentNoVoted(commentId, userId, getAdapterPosition());
				} else {
					commentActionListener.commentVotedAgree(commentId, userId,
															getAdapterPosition());
				}
				break;
			case R.id.vote_disagree:
				if (voteType == CommentVoteType.DISAGREE) {
					commentActionListener.commentNoVoted(commentId, userId, getAdapterPosition());
				} else {
					commentActionListener.commentVotedDisagree(commentId, userId,
															   getAdapterPosition());
				}
				break;
			case R.id.button_delete:
				if (Application.getSeeAdmin()) {
					commentActionListener.commentDelete(commentId, comment.getText().toString(),
														getAdapterPosition());
				}
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
				voteAgreeButton.setTextColor(colorNormal);
				voteAgreeButton.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUp, null, null,
																				null);
				voteDisagreeButton.setTextColor(colorNormal);
				voteDisagreeButton.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDown, null,
																				   null, null);
			} else {
				switch (voteType) {
					case AGREE:
						voteAgreeButton.setTextColor(colorAccent);
						voteAgreeButton
						  .setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUpAccent, null,
																		   null, null);
						voteDisagreeButton.setTextColor(colorNormal);
						voteDisagreeButton
						  .setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDown, null, null,
																		   null);
						break;
					case DISAGREE:
						voteAgreeButton.setTextColor(colorNormal);
						voteAgreeButton.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUp,
																						null, null,
																						null);
						voteDisagreeButton.setTextColor(colorAccent);
						voteDisagreeButton
						  .setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDownAccent, null,
																		   null, null);
						break;
				}
			}
		} else {
			if (voteType == null) {
				voteAgreeButton.setTextColor(colorNormal);
				voteAgreeButton.setCompoundDrawablesWithIntrinsicBounds(thumbUp, null, null, null);
				voteDisagreeButton.setTextColor(colorNormal);
				voteDisagreeButton.setCompoundDrawablesWithIntrinsicBounds(thumbDown, null, null,
																		   null);
			} else {
				switch (voteType) {
					case AGREE:
						voteAgreeButton.setTextColor(colorAccent);
						voteAgreeButton.setCompoundDrawablesWithIntrinsicBounds(thumbUpAccent, null,
																				null, null);
						voteDisagreeButton.setTextColor(colorNormal);
						voteDisagreeButton.setCompoundDrawablesWithIntrinsicBounds(thumbDown, null,
																				   null, null);
						break;
					case DISAGREE:
						voteAgreeButton.setTextColor(colorNormal);
						voteAgreeButton.setCompoundDrawablesWithIntrinsicBounds(thumbUp, null, null,
																				null);
						voteDisagreeButton.setTextColor(colorAccent);
						voteDisagreeButton.setCompoundDrawablesWithIntrinsicBounds(thumbDownAccent,
																				   null, null,
																				   null);
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
