package com.livae.ff.app.viewholders;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.CommentActionListener;
import com.livae.ff.app.utils.ImageUtils;
import com.livae.ff.app.utils.UnitUtils;
import com.livae.ff.app.view.AnonymousImage;
import com.livae.ff.common.Constants.CommentVoteType;

public class CommentViewHolder extends RecyclerView.ViewHolder
  implements View.OnClickListener, View.OnLongClickListener {

	private Long commentId;

	private AnonymousImage anonymousImage;

	private ImageView userImage;

	private TextView userAlias;

	private TextView comment;

	private TextView date;

	private CommentActionListener commentActionListener;

	private View extraPadding;

	private View arrow;

	private View progressBar;

	public CommentViewHolder(View itemView, CommentActionListener commentActionListener) {
		super(itemView);
		this.commentActionListener = commentActionListener;
		anonymousImage = (AnonymousImage) itemView.findViewById(R.id.anonymous_image);
		userImage = (ImageView) itemView.findViewById(R.id.user_image);
		userAlias = (TextView) itemView.findViewById(R.id.anonymous_name);
		comment = (TextView) itemView.findViewById(R.id.comment);
		date = (TextView) itemView.findViewById(R.id.comment_date);
		extraPadding = itemView.findViewById(R.id.extra_padding);
		arrow = itemView.findViewById(R.id.comment_arrow);
		progressBar = itemView.findViewById(R.id.progress_bar);
//		voteAgreeButton = (Button) itemView.findViewById(R.id.vote_agree);
//		voteDisagreeButton = (Button) itemView.findViewById(R.id.vote_disagree);
//		voteAgreeButton.setOnClickListener(this);
//		voteDisagreeButton.setOnClickListener(this);
//		if (Application.getSeeAdmin()) {
//			Button buttonDelete = (Button) itemView.findViewById(R.id.button_delete);
//			buttonDelete.setOnClickListener(this);
//			buttonDelete.setVisibility(View.VISIBLE);
//		}
	}

	public void clear() {
		if (anonymousImage != null) {
			anonymousImage.setSeed(0);
		}
		if (userAlias != null) {
			userAlias.setVisibility(View.VISIBLE);
		}
		if (userAlias != null) {
			userAlias.setText(null);
		}
		if (progressBar != null) {
			progressBar.setVisibility(View.GONE);
		}
		if (userImage != null) {
			userImage.setVisibility(View.VISIBLE);
			userImage.setImageResource(R.drawable.ic_account_circle_white_48dp);
		}
		arrow.setVisibility(View.VISIBLE);
		comment.setText(null);
		date.setText(null);
		date.setVisibility(View.VISIBLE);
//		comment.setOnLongClickListener(null);
	}

	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}

	public void setAnonymousImageSeed(String seed) {
		if (anonymousImage != null) {
			anonymousImage.setVisibility(View.VISIBLE);
			anonymousImage.setSeed(seed);
		}
	}

	public void setAnonymousImageSeed(long seed) {
		if (anonymousImage != null) {
			anonymousImage.setVisibility(View.VISIBLE);
			if (userImage != null) {
				userImage.setVisibility(View.INVISIBLE);
			}
			anonymousImage.setSeed(seed);
			if (seed == 0) {
				anonymousImage.setBackgroundResource(R.drawable.me_drawable);
			} else {
				anonymousImage.setBackgroundResource(0);
			}
		}
	}

	public void setAnonymousNick(String alias) {
		if (userAlias != null) {
			userAlias.setText(alias);
		}
	}

	public void setComment(String comment, long date) {
		final CharSequence dateString = UnitUtils.getAgoTime(this.date.getContext(), date);
		this.date.setText(dateString);
		final String sufix = " " + dateString;
		comment = comment + sufix;
		Spannable span = new SpannableString(comment);
		span.setSpan(new ForegroundColorSpan(Color.TRANSPARENT), comment.length() - sufix.length(),
					 comment.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		this.comment.setText(span);
	}

	public void setVotes(int upVotes, int downVotes) {
//		voteAgreeButton.setText(Integer.toString(upVotes));
//		voteDisagreeButton.setText(Integer.toString(downVotes));
	}

	public void setCanVote(boolean canVote) {
//		voteAgreeButton.setEnabled(canVote);
//		voteDisagreeButton.setEnabled(canVote);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
//			case R.id.vote_agree:
//				if (voteType == CommentVoteType.AGREE) {
//					commentActionListener.commentNoVoted(commentId, userId, getAdapterPosition());
//				} else {
//					commentActionListener.commentVotedAgree(commentId, userId,
//															getAdapterPosition());
//				}
//				break;
//			case R.id.vote_disagree:
//				if (voteType == CommentVoteType.DISAGREE) {
//					commentActionListener.commentNoVoted(commentId, userId, getAdapterPosition());
//				} else {
//					commentActionListener.commentVotedDisagree(commentId, userId,
//															   getAdapterPosition());
//				}
//				break;
//			case R.id.button_delete:
//				if (Application.getSeeAdmin()) {
//					commentActionListener.commentDelete(commentId, comment.getText().toString(),
//														getAdapterPosition());
//				}
//				break;
		}
	}

	public void setVoteType(CommentVoteType voteType) {
//		this.voteType = voteType;
//		Resources resources = itemView.getContext().getResources();
//		int colorAccent = resources.getColor(R.color.accent);
//		int colorNormal = resources.getColor(R.color.black);
//		Drawable thumbUp = resources.getDrawable(R.drawable.ic_action_thumb_up);
//		Drawable thumbDown = resources.getDrawable(R.drawable.ic_action_thumb_down);
//		Drawable thumbUpAccent = resources.getDrawable(R.drawable.ic_action_thumb_up_accent);
//		Drawable thumbDownAccent = resources.getDrawable(R.drawable.ic_action_thumb_down_accent);
//		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
//			if (voteType == null) {
//				voteAgreeButton.setTextColor(colorNormal);
//				voteAgreeButton.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUp, null, null,
//																				null);
//				voteDisagreeButton.setTextColor(colorNormal);
//				voteDisagreeButton.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDown, null,
//																				   null, null);
//			} else {
//				switch (voteType) {
//					case AGREE:
//						voteAgreeButton.setTextColor(colorAccent);
//						voteAgreeButton
//						  .setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUpAccent, null,
//																		   null, null);
//						voteDisagreeButton.setTextColor(colorNormal);
//						voteDisagreeButton
//						  .setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDown, null, null,
//																		   null);
//						break;
//					case DISAGREE:
//						voteAgreeButton.setTextColor(colorNormal);
//						voteAgreeButton.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUp,
//																						null, null,
//																						null);
//						voteDisagreeButton.setTextColor(colorAccent);
//						voteDisagreeButton
//						  .setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDownAccent, null,
//																		   null, null);
//						break;
//				}
//			}
//		} else {
//			if (voteType == null) {
//				voteAgreeButton.setTextColor(colorNormal);
//				voteAgreeButton.setCompoundDrawablesWithIntrinsicBounds(thumbUp, null, null, null);
//				voteDisagreeButton.setTextColor(colorNormal);
//				voteDisagreeButton.setCompoundDrawablesWithIntrinsicBounds(thumbDown, null, null,
//																		   null);
//			} else {
//				switch (voteType) {
//					case AGREE:
//						voteAgreeButton.setTextColor(colorAccent);
//						voteAgreeButton.setCompoundDrawablesWithIntrinsicBounds(thumbUpAccent, null,
//																				null, null);
//						voteDisagreeButton.setTextColor(colorNormal);
//						voteDisagreeButton.setCompoundDrawablesWithIntrinsicBounds(thumbDown, null,
//																				   null, null);
//						break;
//					case DISAGREE:
//						voteAgreeButton.setTextColor(colorNormal);
//						voteAgreeButton.setCompoundDrawablesWithIntrinsicBounds(thumbUp, null, null,
//																				null);
//						voteDisagreeButton.setTextColor(colorAccent);
//						voteDisagreeButton.setCompoundDrawablesWithIntrinsicBounds(thumbDownAccent,
//																				   null, null,
//																				   null);
//						break;
//				}
//			}
//		}
	}

	@Override
	public boolean onLongClick(View v) {
//		commentActionListener.commentUpdate(commentId, comment.getText().toString(),
//											getAdapterPosition());
		return true;
	}

	public void setSending(boolean sending) {
		if (progressBar != null) {
			if (sending) {
				progressBar.setVisibility(View.VISIBLE);
				date.setVisibility(View.GONE);
			} else {
				progressBar.setVisibility(View.GONE);
				date.setVisibility(View.VISIBLE);
			}
		}
	}

	public void setExtraPadding(boolean extra) {
		if (extra) {
			extraPadding.setVisibility(View.VISIBLE);
			arrow.setVisibility(View.VISIBLE);
			if (anonymousImage != null) {
				anonymousImage.setVisibility(View.VISIBLE);
			}
			if (userAlias != null) {
				userAlias.setVisibility(View.VISIBLE);
			}
		} else {
			extraPadding.setVisibility(View.GONE);
			arrow.setVisibility(View.GONE);
			if (anonymousImage != null) {
				anonymousImage.setVisibility(View.INVISIBLE);
			}
			if (userAlias != null) {
				userAlias.setVisibility(View.GONE);
			}
			if (userImage != null) {
				userImage.setVisibility(View.INVISIBLE);
			}
		}
	}

	public void setUserImageUri(String userImageUri) {
		if (userImage != null) {
			userImage.setVisibility(View.VISIBLE);
			if (anonymousImage != null) {
				anonymousImage.setVisibility(View.INVISIBLE);
			}
			ImageUtils.loadUserImage(userImage, userImageUri);
		}
	}
}
