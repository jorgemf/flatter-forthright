package com.livae.ff.app.ui.viewholders;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.CommentClickListener;
import com.livae.ff.app.ui.view.AnonymousImage;
import com.livae.ff.app.utils.ImageUtils;
import com.livae.ff.app.utils.UnitUtils;
import com.livae.ff.common.Constants;
import com.livae.ff.common.Constants.CommentVoteType;

public class CommentViewHolder extends RecyclerView.ViewHolder
  implements View.OnLongClickListener, View.OnClickListener {

	private final static ForegroundColorSpan TRANSPARENT_SPAM =
	  new ForegroundColorSpan(Color.TRANSPARENT);

	private static final StyleSpan ITALIC_SPAN = new StyleSpan(Typeface.ITALIC);

	private Long commentId;

	private String commentText;

	private AnonymousImage anonymousImage;

	private ImageView userImage;

	private TextView userAlias;

	private TextView comment;

	private TextView date;

	private TextView dayDate;

	private TextView agree;

	private TextView disagree;

	private TextView userOpinion;

	private TextView commentFlag;

	private TextView userFlag;

	private View extraPadding;

	private View arrow;

	private View progressBar;

	private CommentClickListener longClickListener;

	public CommentViewHolder(View itemView, CommentClickListener longClickListener) {
		super(itemView);
		anonymousImage = (AnonymousImage) itemView.findViewById(R.id.anonymous_image);
		userImage = (ImageView) itemView.findViewById(R.id.user_image);
		userAlias = (TextView) itemView.findViewById(R.id.anonymous_name);
		comment = (TextView) itemView.findViewById(R.id.comment);
		date = (TextView) itemView.findViewById(R.id.comment_date);
		dayDate = (TextView) itemView.findViewById(R.id.day_date);
		extraPadding = itemView.findViewById(R.id.extra_padding);
		arrow = itemView.findViewById(R.id.comment_arrow);
		progressBar = itemView.findViewById(R.id.progress_bar);
		agree = (TextView) itemView.findViewById(R.id.text_agree);
		disagree = (TextView) itemView.findViewById(R.id.text_disagree);
		userOpinion = (TextView) itemView.findViewById(R.id.text_user_agree_disagree);
		commentFlag = (TextView) itemView.findViewById(R.id.user_flagged);
		userFlag = (TextView) itemView.findViewById(R.id.comment_flagged);
		this.longClickListener = longClickListener;
		itemView.setOnLongClickListener(this);
		itemView.setOnClickListener(this);
	}

	public void setSelected(boolean selected) {
		itemView.setSelected(selected);
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
		dayDate.setVisibility(View.GONE);
		dayDate.setText(null);
		if (agree != null) {
			agree.setVisibility(View.GONE);
		}
		if (disagree != null) {
			disagree.setVisibility(View.GONE);
		}
		if (userOpinion != null) {
			userOpinion.setVisibility(View.GONE);
		}
		if (commentFlag != null) {
			commentFlag.setVisibility(View.GONE);
		}
		if (userFlag != null) {
			userFlag.setVisibility(View.GONE);
		}
		this.commentText = null;
		this.commentId = null;
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

	public void setComment(String comment, long date, Long previousDate) {
		final CharSequence dateString = UnitUtils.getTime(this.date.getContext(), date);
		this.commentText = comment;
		if (comment == null) {
			comment = this.comment.getContext().getString(R.string.hidden_comment);
		}
		this.date.setText(dateString);
		final String sufix = " " + dateString;
		comment = comment + sufix;
		Spannable span = new SpannableString(comment);
		span.setSpan(TRANSPARENT_SPAM, comment.length() - sufix.length(), comment.length(),
					 Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		if (this.commentText == null) {
			span.setSpan(ITALIC_SPAN, 0, comment.length() - sufix.length(),
						 Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		this.comment.setText(span);
		if (previousDate == null || !UnitUtils.isItSameDay(date, previousDate)) {
			dayDate.setVisibility(View.VISIBLE);
			dayDate.setText(UnitUtils.getDate(dayDate.getContext(), date));
		}
	}

	public void setVotes(int upVotes, int downVotes) {
		if (upVotes > 0) {
			agree.setText(Integer.toString(upVotes));
			agree.setVisibility(View.VISIBLE);
		}
		if (downVotes > 0) {
			disagree.setText(Integer.toString(downVotes));
			disagree.setVisibility(View.VISIBLE);
		}
	}

	public void setUserVoteType(CommentVoteType voteType, String userName) {
		if (voteType == null) {
			userOpinion.setVisibility(View.GONE);
		} else {
			userOpinion.setVisibility(View.VISIBLE);
			userName = userName.trim();
			int space = userName.indexOf(' ');
			if (space > 0) {
				userName = userName.substring(0, space);
			}
			final Resources resources = userOpinion.getResources();
			Drawable thumbUp;
			Drawable thumbDown;
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
				Resources.Theme theme = userOpinion.getContext().getTheme();
				thumbUp = theme.getDrawable(R.drawable.ic_thumb_up_terciary_14dp);
				thumbDown = theme.getDrawable(R.drawable.ic_thumb_down_terciary_14dp);
			} else {
				//noinspection deprecation
				thumbUp = resources.getDrawable(R.drawable.ic_thumb_up_terciary_14dp);
				//noinspection deprecation
				thumbDown = resources.getDrawable(R.drawable.ic_thumb_down_terciary_14dp);
			}
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
				switch (voteType) {
					case AGREE:
						userOpinion.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUp, null,
																					null, null);
						userOpinion.setText(resources.getString(R.string.user_agree, userName));
						break;
					case DISAGREE:
						userOpinion.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDown,
																					null,
																					null, null);
						userOpinion.setText(resources.getString(R.string.user_disagree, userName));
						break;
				}
			} else {
				switch (voteType) {
					case AGREE:
						userOpinion.setCompoundDrawablesWithIntrinsicBounds(thumbUp, null, null,
																			null);
						userOpinion.setText(resources.getString(R.string.user_agree, userName));
						break;
					case DISAGREE:
						userOpinion.setCompoundDrawablesWithIntrinsicBounds(thumbDown, null, null,
																			null);
						userOpinion.setText(resources.getString(R.string.user_disagree, userName));
						break;
				}
			}
		}
	}

	public void setVoteType(CommentVoteType voteType) {
		final Resources resources = itemView.getContext().getResources();
		final int colorAccent = resources.getColor(R.color.black);
		final int colorNormal = resources.getColor(R.color.grey_light);
		Drawable thumbUp;
		Drawable thumbDown;
		Drawable thumbUpAccent;
		Drawable thumbDownAccent;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			Resources.Theme theme = userOpinion.getContext().getTheme();
			thumbUp = theme.getDrawable(R.drawable.ic_thumb_up_terciary_14dp);
			thumbDown = theme.getDrawable(R.drawable.ic_thumb_down_terciary_14dp);
			thumbUpAccent = theme.getDrawable(R.drawable.ic_thumb_up_black_14dp);
			thumbDownAccent = theme.getDrawable(R.drawable.ic_thumb_down_black_14dp);
		} else {
			//noinspection deprecation
			thumbUp = resources.getDrawable(R.drawable.ic_thumb_up_terciary_14dp);
			//noinspection deprecation
			thumbDown = resources.getDrawable(R.drawable.ic_thumb_down_terciary_14dp);
			//noinspection deprecation
			thumbUpAccent = resources.getDrawable(R.drawable.ic_thumb_up_black_14dp);
			//noinspection deprecation
			thumbDownAccent = resources.getDrawable(R.drawable.ic_thumb_down_black_14dp);
		}
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
			if (voteType == null) {
				agree.setTextColor(colorNormal);
				agree.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUp, null, null, null);
				disagree.setTextColor(colorNormal);
				disagree.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDown, null, null,
																		 null);
			} else {
				switch (voteType) {
					case AGREE:
						agree.setTextColor(colorAccent);
						agree.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUpAccent, null,
																			  null, null);
						disagree.setTextColor(colorNormal);
						disagree.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDown, null,
																				 null, null);
						break;
					case DISAGREE:
						agree.setTextColor(colorNormal);
						agree.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbUp, null, null,
																			  null);
						disagree.setTextColor(colorAccent);
						disagree.setCompoundDrawablesRelativeWithIntrinsicBounds(thumbDownAccent,
																				 null, null, null);
						break;
				}
			}
		} else {
			if (voteType == null) {
				agree.setTextColor(colorNormal);
				agree.setCompoundDrawablesWithIntrinsicBounds(thumbUp, null, null, null);
				disagree.setTextColor(colorNormal);
				disagree.setCompoundDrawablesWithIntrinsicBounds(thumbDown, null, null, null);
			} else {
				switch (voteType) {
					case AGREE:
						agree.setTextColor(colorAccent);
						agree.setCompoundDrawablesWithIntrinsicBounds(thumbUpAccent, null, null,
																	  null);
						disagree.setTextColor(colorNormal);
						disagree.setCompoundDrawablesWithIntrinsicBounds(thumbDown, null, null,
																		 null);
						break;
					case DISAGREE:
						agree.setTextColor(colorNormal);
						agree.setCompoundDrawablesWithIntrinsicBounds(thumbUp, null, null, null);
						disagree.setTextColor(colorAccent);
						disagree.setCompoundDrawablesWithIntrinsicBounds(thumbDownAccent, null,
																		 null, null);
						break;
				}
			}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		if (longClickListener != null) {
			return longClickListener.onLongClick(this);
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		if (longClickListener != null) {
			longClickListener.onClick(this);
		}
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
		} else {
			extraPadding.setVisibility(View.GONE);
		}
	}

	public void setFirstCommentOfPerson(boolean first) {
		if (first) {
			arrow.setVisibility(View.VISIBLE);
			if (anonymousImage != null) {
				anonymousImage.setVisibility(View.VISIBLE);
			}
			if (userAlias != null) {
				userAlias.setVisibility(View.VISIBLE);
			}
		} else {
			arrow.setVisibility(View.GONE);
			if (anonymousImage != null) {
				anonymousImage.setVisibility(View.INVISIBLE);
			}
			if (userAlias != null) {
				userAlias.setVisibility(View.GONE);
			}
			if (userFlag != null) {
				userFlag.setVisibility(View.GONE);
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

	public void setUserMark(Constants.UserMark userMark) {
		if (userMark == null) {
			userFlag.setVisibility(View.GONE);
		} else {
			userFlag.setVisibility(View.VISIBLE);
			switch (userMark) {
				case BULLY:
					userFlag.setText(R.string.flagged_user_abuse);
					break;
				case TROLL:
					userFlag.setText(R.string.flagged_user_insult);
					break;
				case LIAR:
					userFlag.setText(R.string.flagged_user_lie);
					break;
				case CONTROVERSIAL:
				default:
					userFlag.setText(R.string.flagged_user_other);
					break;
			}
		}
	}

	public void setCommentFlag(Constants.FlagReason flagReason) {
		if (flagReason == null) {
			commentFlag.setVisibility(View.GONE);
		} else {
			commentFlag.setVisibility(View.VISIBLE);
			switch (flagReason) {
				case ABUSE:
					commentFlag.setText(R.string.flagged_comment_abuse);
					break;
				case INSULT:
					commentFlag.setText(R.string.flagged_comment_insult);
					break;
				case LIE:
					commentFlag.setText(R.string.flagged_comment_lie);
					break;
				case OTHER:
				default:
					commentFlag.setText(R.string.flagged_comment_other);
					break;
			}
		}
	}

	public Long getCommentId() {
		return commentId;
	}

	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}

	public String getComment() {
		return commentText;
	}
}
