package com.livae.ff.app.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livae.android.loading.CursorRecyclerAdapter;
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
		long appId = cursor.getLong(iAppId);
		long userId = cursor.getLong(iUserId);
		if (showApp) {
			holder.setAppId(appId);
			holder.setAppImageUrl(cursor.getString(iAppImageUrl));
			holder.setTitle(cursor.getString(iAppTitle));
			holder.setSubtitle(cursor.getString(iAppDescription));
		} else {
			holder.setUserId(userId);
			String userName = cursor.getString(iUserName);
			if (userName == null || userName.length() == 0 || cursor.getInt(iUserAnonymous) != 0) {
				holder.setUserAnonymous();
			} else {
				holder.setTitle(userName);
				holder.setSubtitle(cursor.getString(iUserTagline));
				holder.setUserImageUrl(cursor.getString(iUserImageUrl));
			}
			Relationship relationship = null;
			String relationshipString = cursor.getString(iRelationship);
			if (relationshipString != null) {
				try {
					relationship = Relationship.valueOf(relationshipString);
				} catch (Exception ignore) {
				}
			}
			holder.setUserRelationship(relationship);
		}
		long date = cursor.getLong(iDate);
		holder.setDate(date);
		if (commentsUpdated.containsKey(commentId)) {
			holder.setComment(commentsUpdated.get(commentId));
		} else {
			holder.setComment(cursor.getString(iComment));
		}
		int upVotes = cursor.getInt(iUpVotes);
		int downVotes = cursor.getInt(iDownVotes);
		holder.setCanVote(!userAnonymous && userId != this.userId);
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
				if (previousVoteType == CommentVoteType.UP) {
					upVotes -= 1;
				} else if (previousVoteType == CommentVoteType.DOWN) {
					downVotes -= 1;
				}
			} else if (voteType == CommentVoteType.UP) {
				if (previousVoteType == null) {
					upVotes += 1;
				} else if (previousVoteType == CommentVoteType.DOWN) {
					upVotes += 1;
					downVotes -= 1;
				}
			} else if (voteType == CommentVoteType.DOWN) {
				if (previousVoteType == null) {
					downVotes += 1;
				} else if (previousVoteType == CommentVoteType.UP) {
					downVotes += 1;
					upVotes -= 1;
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
		holder.setVotes(upVotes, downVotes);
		holder.setVoteType(voteType);
	}
}
