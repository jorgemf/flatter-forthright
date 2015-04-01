package com.livae.ff.app.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livae.android.loading.CursorRecyclerAdapter;
import com.livae.apphunt.app.AppUser;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.R;
import com.livae.apphunt.app.listener.ApplicationClickListener;
import com.livae.apphunt.app.listener.CommentActionListener;
import com.livae.apphunt.app.listener.UserClickListener;
import com.livae.apphunt.app.sql.Table;
import com.livae.apphunt.app.viewholders.CommentsViewHolder;
import com.livae.apphunt.common.Constants.CommentVoteType;
import com.livae.apphunt.common.Constants.Relationship;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public class CommentsAdapter extends CursorRecyclerAdapter<CommentsViewHolder> {

	public static final String[] PROJECTION = {Table.Comment.T_ID, Table.Comment.USER_ID,
											   Table.Comment.APPLICATION_ENTRY_ID,
											   Table.Comment.DATE, Table.Comment.UP_VOTES,
											   Table.Comment.DOWN_VOTES, Table.Comment.COMMENT,
											   Table.Comment.VOTE_TYPE, Table.User.IMAGE_URL,
											   Table.User.USER_NAME, Table.User.TAGLINE,
											   Table.User.ANONYMOUS, Table.AppEntry.TITLE,
											   Table.AppEntry.DESCRIPTION, Table.AppEntry.IMAGE_URL,
											   Table.UserApplicationRelated.RELATIONSHIP};

	private int iId;

	private int iUserId;

	private int iUserImageUrl;

	private int iUserName;

	private int iUserTagline;

	private int iUserAnonymous;

	private int iAppId;

	private int iAppImageUrl;

	private int iAppTitle;

	private int iAppDescription;

	private int iDate;

	private int iUpVotes;

	private int iDownVotes;

	private int iComment;

	private int iVoteType;

	private int iRelationship;

	private CommentActionListener commentActionListener;

	private UserClickListener userClickListener;

	private ApplicationClickListener applicationClickListener;

	private LayoutInflater layoutInflater;

	private boolean userAnonymous;

	private Long userId;

	private HashMap<Long, CommentVoteType> commentVoteTypeHashMap;

	private boolean showApp;

	private long updateCommentWindow;

	private HashMap<Long, String> commentsUpdated;

	public CommentsAdapter(@Nonnull Activity activity,
						   @Nonnull CommentActionListener commentActionListener,
						   @Nonnull UserClickListener userClickListener,
						   @Nonnull ApplicationClickListener applicationClickListener) {
		layoutInflater = activity.getLayoutInflater();
		this.commentActionListener = commentActionListener;
		this.userClickListener = userClickListener;
		this.applicationClickListener = applicationClickListener;
		AppUser appUser = Application.appUser();
		userAnonymous = appUser.isAnonymous();
		userId = appUser.getUserId();
		commentVoteTypeHashMap = new HashMap<>();
		commentsUpdated = new HashMap<>();
		showApp = false;
		int minutes = activity.getResources().getInteger(R.integer.comment_update_time_minutes);
		updateCommentWindow = TimeUnit.MINUTES.toMillis(minutes);
	}

	@Override
	public CommentsViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int type) {
		View view = layoutInflater.inflate(R.layout.item_comment, viewGroup, false);
		if (showApp) {
			return new CommentsViewHolder(view, commentActionListener, applicationClickListener);
		} else {
			return new CommentsViewHolder(view, commentActionListener, userClickListener);
		}
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
		iUserImageUrl = cursor.getColumnIndex(Table.User.IMAGE_URL);
		iUserName = cursor.getColumnIndex(Table.User.USER_NAME);
		iUserTagline = cursor.getColumnIndex(Table.User.TAGLINE);
		iUserAnonymous = cursor.getColumnIndex(Table.User.ANONYMOUS);
		iAppId = cursor.getColumnIndex(Table.Comment.APPLICATION_ENTRY_ID);
		iAppImageUrl = cursor.getColumnIndex(Table.AppEntry.IMAGE_URL);
		iAppTitle = cursor.getColumnIndex(Table.AppEntry.TITLE);
		iAppDescription = cursor.getColumnIndex(Table.AppEntry.DESCRIPTION);
		iDate = cursor.getColumnIndex(Table.Comment.DATE);
		iUpVotes = cursor.getColumnIndex(Table.Comment.UP_VOTES);
		iDownVotes = cursor.getColumnIndex(Table.Comment.DOWN_VOTES);
		iComment = cursor.getColumnIndex(Table.Comment.COMMENT);
		iVoteType = cursor.getColumnIndex(Table.Comment.VOTE_TYPE);
		iRelationship = cursor.getColumnIndex(Table.UserApplicationRelated.RELATIONSHIP);
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
		holder.canBeUpdated(userId == this.userId &&
							date + updateCommentWindow > System.currentTimeMillis());
	}

	public void setShowApp(boolean showApp) {
		this.showApp = showApp;
	}

	public void setCommentText(Long id, String comment) {
		commentsUpdated.put(id, comment);
	}
}
