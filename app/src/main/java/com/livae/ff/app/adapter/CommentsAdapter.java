package com.livae.ff.app.adapter;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.fragment.AbstractLoaderFragment;
import com.livae.ff.app.listener.CommentActionListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.viewholders.CommentViewHolder;
import com.livae.ff.common.Constants.ChatType;
import com.livae.ff.common.Constants.CommentVoteType;

import java.util.HashMap;

import javax.annotation.Nonnull;

public class CommentsAdapter extends EndlessCursorAdapter<CommentViewHolder> {

	public static final String[] PROJECTION = {Table.Comment.T_ID, Table.Comment.DATE,
											   Table.Comment.CONVERSATION_ID,
											   Table.Comment.USER_ANONYMOUS_ID,
											   Table.Comment.USER_ALIAS, Table.Comment.IS_ME,
											   Table.Comment.COMMENT, Table.Comment.AGREE_VOTES,
											   Table.Comment.DISAGREE_VOTES,
											   Table.Comment.USER_VOTE_TYPE,
											   Table.Comment.VOTE_TYPE, Table.Comment.USER_MARK,
											   Table.Comment.TIMES_FLAGGED,
											   Table.Comment.TIMES_FLAGGED_ABUSE,
											   Table.Comment.TIMES_FLAGGED_INSULT,
											   Table.Comment.TIMES_FLAGGED_LIE,
											   Table.Comment.TIMES_FLAGGED_OTHER};

	private int iId;

	private int iDate;

	private int iUserAnonymousId;

	private int iUserAlias;

	private int iIsMe;

	private int iComment;

	private int iAgreeVotes;

	private int iDisagreeVotes;

	private int iVoteType;

	private int iUserVoteType;

	private int iUserMark;

	private int iTimesFlagged;

	private int iTimesFlaggedAbuse;

	private int iTimesFlaggedInsult;

	private int iTimesFlaggedLie;

	private int iTimesFlaggedOther;

	private CommentActionListener commentActionListener;

	private LayoutInflater layoutInflater;

	private ChatType conversationType;

	private HashMap<Long, CommentVoteType> commentVoteTypeHashMap;

	public CommentsAdapter(@Nonnull AbstractLoaderFragment fragment,
						   @Nonnull CommentActionListener commentActionListener,
						   ChatType conversationType) {
		super(fragment.getActivity(), fragment);
		layoutInflater = fragment.getActivity().getLayoutInflater();
		this.commentActionListener = commentActionListener;
		commentVoteTypeHashMap = new HashMap<>();
		this.conversationType = conversationType;
	}

	public void votedComment(Long commentId, CommentVoteType voteType) {
		commentVoteTypeHashMap.put(commentId, voteType);
	}

	@Override
	public void setCursor(Cursor cursor) {
		commentVoteTypeHashMap.clear();
		super.setCursor(cursor);
	}

	@Override
	public void findIndexes(Cursor cursor) {
		iId = cursor.getColumnIndex(Table.Comment.ID);
		iDate = cursor.getColumnIndex(Table.Comment.DATE);
		iUserAnonymousId = cursor.getColumnIndex(Table.Comment.USER_ANONYMOUS_ID);
		iUserAlias = cursor.getColumnIndex(Table.Comment.USER_ALIAS);
		iIsMe = cursor.getColumnIndex(Table.Comment.IS_ME);
		iComment = cursor.getColumnIndex(Table.Comment.COMMENT);
		iAgreeVotes = cursor.getColumnIndex(Table.Comment.AGREE_VOTES);
		iDisagreeVotes = cursor.getColumnIndex(Table.Comment.DISAGREE_VOTES);
		iVoteType = cursor.getColumnIndex(Table.Comment.VOTE_TYPE);
		iUserVoteType = cursor.getColumnIndex(Table.Comment.USER_VOTE_TYPE);
		iUserMark = cursor.getColumnIndex(Table.Comment.USER_MARK);
		iTimesFlagged = cursor.getColumnIndex(Table.Comment.TIMES_FLAGGED);
		iTimesFlaggedAbuse = cursor.getColumnIndex(Table.Comment.TIMES_FLAGGED_ABUSE);
		iTimesFlaggedInsult = cursor.getColumnIndex(Table.Comment.TIMES_FLAGGED_INSULT);
		iTimesFlaggedLie = cursor.getColumnIndex(Table.Comment.TIMES_FLAGGED_LIE);
		iTimesFlaggedOther = cursor.getColumnIndex(Table.Comment.TIMES_FLAGGED_OTHER);
	}

	@Override
	protected CommentViewHolder createCustomViewHolder(ViewGroup viewGroup, int type) {
		View view = layoutInflater.inflate(R.layout.item_comment_private, viewGroup, false);
		return new CommentViewHolder(view, commentActionListener);
	}

	@Override
	protected void bindCustomViewHolder(CommentViewHolder holder, int position, Cursor cursor) {
		holder.clear();
		long commentId = cursor.getLong(iId);
		holder.setCommentId(commentId);
		long date = cursor.getLong(iDate);
		holder.setDate(date);
		holder.setComment(cursor.getString(iComment));
		int agreeVotes = cursor.getInt(iAgreeVotes);
		int disagreeVotes = cursor.getInt(iDisagreeVotes);
		CommentVoteType voteType = null;
		if (commentVoteTypeHashMap.containsKey(commentId)) {
			voteType = commentVoteTypeHashMap.get(commentId);

			String voteTypeString = cursor.getString(iVoteType);
			CommentVoteType previousVoteType = null;
			if (voteTypeString != null) {
				try {
					previousVoteType = CommentVoteType.valueOf(voteTypeString);
				} catch (Exception ignore) {
				}
			}
			if (voteType == null) {
				if (previousVoteType == CommentVoteType.AGREE) {
					agreeVotes -= 1;
				} else if (previousVoteType == CommentVoteType.DISAGREE) {
					disagreeVotes -= 1;
				}
			} else if (voteType == CommentVoteType.AGREE) {
				if (previousVoteType == null) {
					agreeVotes += 1;
				} else if (previousVoteType == CommentVoteType.DISAGREE) {
					agreeVotes += 1;
					disagreeVotes -= 1;
				}
			} else if (voteType == CommentVoteType.DISAGREE) {
				if (previousVoteType == null) {
					disagreeVotes += 1;
				} else if (previousVoteType == CommentVoteType.AGREE) {
					disagreeVotes += 1;
					agreeVotes -= 1;
				}
			}
		} else {
			String voteTypeString = cursor.getString(iVoteType);
			if (voteTypeString != null) {
				try {
					voteType = CommentVoteType.valueOf(voteTypeString);
				} catch (Exception ignore) {
				}
			}
		}
		holder.setVotes(agreeVotes, disagreeVotes);
		holder.setVoteType(voteType);
	}
}
