package com.livae.ff.app.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.CommentActionListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.viewholders.CommentsViewHolder;
import com.livae.ff.common.Constants.CommentVoteType;

import java.util.HashMap;

import javax.annotation.Nonnull;

public class CommentsAdapter extends EndlessCursorAdapter<CommentsViewHolder> {

	public static final String[] PROJECTION = {Table.Comment.T_ID, Table.Comment.USER_ID,
											   Table.Comment.PHONE, Table.Comment.DATE,
											   Table.Comment.AGREE_VOTES, Table.Comment.VOTE_TYPE,
											   Table.Comment.DISAGREE_VOTES, Table.Comment.COMMENT};

	private int iId;

	private int iUserId;

	private int iPhone;

	private int iDate;

	private int iAgreeVotes;

	private int iDisagreeVotes;

	private int iComment;

	private int iVoteType;

	private CommentActionListener commentActionListener;

	private LayoutInflater layoutInflater;

	private Long userId;

	private HashMap<Long, CommentVoteType> commentVoteTypeHashMap;

	public CommentsAdapter(@Nonnull Activity activity,
						   @Nonnull CommentActionListener commentActionListener) {
		layoutInflater = activity.getLayoutInflater();
		this.commentActionListener = commentActionListener;
		commentVoteTypeHashMap = new HashMap<>();
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	@Override
	public CommentsViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int type) {
		View view = layoutInflater.inflate(R.layout.item_comment, viewGroup, false);
		return new CommentsViewHolder(view, commentActionListener);
	}

	public void votedComment(Long commentId, CommentVoteType voteType) {
		commentVoteTypeHashMap.put(commentId, voteType);
	}

	@Override
	public void changeCursor(Cursor cursor) {
		commentVoteTypeHashMap.clear();
		super.changeCursor(cursor);
	}

	@Override
	public void findIndexes(Cursor cursor) {
		iId = cursor.getColumnIndex(Table.Comment.ID);
		iUserId = cursor.getColumnIndex(Table.Comment.USER_ID);
		iDate = cursor.getColumnIndex(Table.Comment.DATE);
		iAgreeVotes = cursor.getColumnIndex(Table.Comment.AGREE_VOTES);
		iDisagreeVotes = cursor.getColumnIndex(Table.Comment.DISAGREE_VOTES);
		iComment = cursor.getColumnIndex(Table.Comment.COMMENT);
		iVoteType = cursor.getColumnIndex(Table.Comment.VOTE_TYPE);
		iPhone = cursor.getColumnIndex(Table.Comment.PHONE);
	}

	@Override
	public void onBindViewHolder(final CommentsViewHolder holder, final Cursor cursor,
								 final int position) {
		holder.clear();
		long commentId = cursor.getLong(iId);
		holder.setCommentId(commentId);
		long userId = cursor.getLong(iUserId);
		holder.setUserId(userId);
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
