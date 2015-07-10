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
											   Table.Comment.TIMES_FLAGGED_OTHER,
											   Table.CommentSync.TEMP_SYNC};

	private static final int COMMENT_PUBLIC_ME = 1;

	private static final int COMMENT_PUBLIC_OTHERS = 2;

	private static final int COMMENT_PUBLIC_USER = 3;

	private static final int COMMENT_PRIVATE_ME = 4;

	private static final int COMMENT_PRIVATE_OTHERS = 5;

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

	private int iSyncTemp;

	private CommentActionListener commentActionListener;

	private LayoutInflater layoutInflater;

	private ChatType chatType;

	private HashMap<Long, CommentVoteType> commentVoteTypeHashMap;

	public CommentsAdapter(@Nonnull AbstractLoaderFragment fragment,
						   @Nonnull CommentActionListener commentActionListener,
						   @Nonnull ChatType chatType) {
		super(fragment.getActivity(), fragment);
		layoutInflater = fragment.getActivity().getLayoutInflater();
		this.commentActionListener = commentActionListener;
		commentVoteTypeHashMap = new HashMap<>();
		this.chatType = chatType;
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
		iSyncTemp = cursor.getColumnIndex(Table.CommentSync.TEMP_SYNC);
	}

	@Override
	public int getCustomItemViewType(int position, Cursor cursor) {
		boolean isMe = !cursor.isNull(iIsMe) && cursor.getInt(iIsMe) != 0;
		switch (chatType) {
			case FLATTER:
			case FORTHRIGHT:
				if (isMe) {
					return COMMENT_PUBLIC_ME;
				} else {
					return COMMENT_PUBLIC_OTHERS;
				}
//				break;
			case PRIVATE:
			case PRIVATE_ANONYMOUS:
			case SECRET:
				if (isMe) {
					return COMMENT_PRIVATE_ME;
				} else {
					return COMMENT_PRIVATE_OTHERS;
				}
//				break;
		}
		return super.getCustomItemViewType(position, cursor);
	}

	@Override
	protected CommentViewHolder createCustomViewHolder(ViewGroup viewGroup, int type) {
		View view = null;
		switch (type) {
			case COMMENT_PRIVATE_ME:
				view = layoutInflater.inflate(R.layout.item_comment_private_mine, viewGroup, false);
				break;
			case COMMENT_PRIVATE_OTHERS:
				view = layoutInflater.inflate(R.layout.item_comment_private_others, viewGroup,
											  false);
				break;
			case COMMENT_PUBLIC_ME:
				view = layoutInflater.inflate(R.layout.item_comment_public_mine, viewGroup, false);
				break;
			case COMMENT_PUBLIC_OTHERS:
				view = layoutInflater.inflate(R.layout.item_comment_public_others, viewGroup,
											  false);
				break;
			case COMMENT_PUBLIC_USER:
				view = layoutInflater.inflate(R.layout.item_comment_public_user, viewGroup, false);
				break;
		}
		return new CommentViewHolder(view, commentActionListener);
	}

	@Override
	protected void bindCustomViewHolder(CommentViewHolder holder, int position, Cursor cursor) {
		holder.clear();
		// TODO set information about the votes
		Long anonymousId = null;
		if (!cursor.isNull(iUserAnonymousId)) {
			anonymousId = cursor.getLong(iUserAnonymousId);
			holder.setAnonymousImageSeed(anonymousId);
		}
		String alias = null;
		if (!cursor.isNull(iUserAlias)) {
			alias = cursor.getString(iUserAlias);
			holder.setAnonymousNick(alias);
		}
		boolean isMe = cursor.getInt(iIsMe) != 0;
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
		boolean isSyncTemp = false;
		if (iSyncTemp >= 0) {
			isSyncTemp = cursor.getInt(iSyncTemp) != 0;
			holder.setSending(isSyncTemp);
		}
		if (cursor.moveToNext()) {
			boolean nextIsMe = cursor.getInt(iIsMe) != 0;
			Long nextAnonymousId = null;
			if (!cursor.isNull(iUserAnonymousId)) {
				nextAnonymousId = cursor.getLong(iUserAnonymousId);
			}
			String nextAlias = null;
			if (!cursor.isNull(iUserAlias)) {
				nextAlias = cursor.getString(iUserAlias);
			}
			if (isMe && nextIsMe) {
				holder.setExtraPadding(!(alias != null && alias.equals(nextAlias)));
			} else {
				holder.setExtraPadding(!(anonymousId != null &&
										 anonymousId.equals(nextAnonymousId)));
			}
			cursor.moveToPrevious();
		}
	}
}
