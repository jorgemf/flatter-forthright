package com.livae.ff.app.adapter;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livae.ff.app.R;
import com.livae.ff.app.listener.CommentClickListener;
import com.livae.ff.app.settings.Settings;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.viewholders.CommentViewHolder;
import com.livae.ff.common.Constants.ChatType;
import com.livae.ff.common.Constants.CommentVoteType;
import com.livae.ff.common.Constants.FlagReason;
import com.livae.ff.common.Constants.UserMark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

public class CommentsAdapter extends EndlessCursorAdapter<CommentViewHolder> {

	public static final String[] PROJECTION =
	  {Table.Comment.T_ID, Table.Comment.DATE, Table.Comment.CONVERSATION_ID,
	   Table.Comment.USER_ANONYMOUS_ID, Table.Comment.USER_ALIAS, Table.Comment.IS_ME,
	   Table.Comment.COMMENT, Table.Comment.AGREE_VOTES, Table.Comment.DISAGREE_VOTES,
	   Table.Comment.USER_VOTE_TYPE, Table.Comment.VOTE_TYPE, Table.Comment.USER_MARK,
	   Table.Comment.TIMES_FLAGGED, Table.Comment.TIMES_FLAGGED_ABUSE,
	   Table.Comment.TIMES_FLAGGED_INSULT, Table.Comment.TIMES_FLAGGED_LIE,
	   Table.Comment.TIMES_FLAGGED_OTHER, Table.CommentSync.TEMP_SYNC};

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

	private LayoutInflater layoutInflater;

	private ChatType chatType;

	private HashMap<Long, CommentVoteType> commentVoteTypeHashMap;

	private boolean isPublicChat;

	private String userImageUri;

	private String userName;

	private CommentClickListener commentClickListener;

	private SparseBooleanArray selectedItems;

	private boolean isMyPublicChat;

	public CommentsAdapter(@Nonnull Fragment fragment,
						   @Nonnull ViewCreator viewCreator,
						   CommentClickListener commentClickListener,
						   @Nonnull ChatType chatType,
						   String userName,
						   String userImageUri,
						   boolean myPublicChat) {
		super(fragment.getActivity(), viewCreator);
		layoutInflater = fragment.getActivity().getLayoutInflater();
		commentVoteTypeHashMap = new HashMap<>();
		this.chatType = chatType;
		isPublicChat = chatType == ChatType.FORTHRIGHT || chatType == ChatType.FLATTER;
		this.userName = userName;
		this.userImageUri = userImageUri;
		this.commentClickListener = commentClickListener;
		selectedItems = new SparseBooleanArray();
		isMyPublicChat = myPublicChat;
	}

	public void votedComment(Long commentId, CommentVoteType voteType) {
		commentVoteTypeHashMap.put(commentId, voteType);
	}

	public void removeVote(Long commentId) {
		commentVoteTypeHashMap.remove(commentId);
	}

	@Override
	public void setCursor(Cursor cursor) {
		commentVoteTypeHashMap.clear();
		super.setCursor(cursor);
	}

	@Override
	public void findIndexes(Cursor cursor) {
		iId = cursor.getColumnIndexOrThrow(Table.Comment.ID);
		iDate = cursor.getColumnIndexOrThrow(Table.Comment.DATE);
		iUserAnonymousId = cursor.getColumnIndexOrThrow(Table.Comment.USER_ANONYMOUS_ID);
		iUserAlias = cursor.getColumnIndexOrThrow(Table.Comment.USER_ALIAS);
		iIsMe = cursor.getColumnIndexOrThrow(Table.Comment.IS_ME);
		iComment = cursor.getColumnIndexOrThrow(Table.Comment.COMMENT);
		iAgreeVotes = cursor.getColumnIndexOrThrow(Table.Comment.AGREE_VOTES);
		iDisagreeVotes = cursor.getColumnIndexOrThrow(Table.Comment.DISAGREE_VOTES);
		iVoteType = cursor.getColumnIndexOrThrow(Table.Comment.VOTE_TYPE);
		iUserVoteType = cursor.getColumnIndexOrThrow(Table.Comment.USER_VOTE_TYPE);
		iUserMark = cursor.getColumnIndexOrThrow(Table.Comment.USER_MARK);
		iTimesFlagged = cursor.getColumnIndexOrThrow(Table.Comment.TIMES_FLAGGED);
		iTimesFlaggedAbuse = cursor.getColumnIndexOrThrow(Table.Comment.TIMES_FLAGGED_ABUSE);
		iTimesFlaggedInsult = cursor.getColumnIndexOrThrow(Table.Comment.TIMES_FLAGGED_INSULT);
		iTimesFlaggedLie = cursor.getColumnIndexOrThrow(Table.Comment.TIMES_FLAGGED_LIE);
		iTimesFlaggedOther = cursor.getColumnIndexOrThrow(Table.Comment.TIMES_FLAGGED_OTHER);
		iSyncTemp = cursor.getColumnIndexOrThrow(Table.CommentSync.TEMP_SYNC);
	}

	@Override
	public int getCustomItemViewType(int position, Cursor cursor) {
		boolean isMe = !cursor.isNull(iIsMe) && cursor.getInt(iIsMe) != 0;
		boolean isTheUser = cursor.isNull(iUserAnonymousId);
		switch (chatType) {
			case FLATTER:
			case FORTHRIGHT:
				if (isMe) {
					return COMMENT_PUBLIC_ME;
				} else if (isTheUser) {
					return COMMENT_PUBLIC_USER;
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
				view = layoutInflater.inflate(R.layout.item_comment_private_mine, viewGroup,
											  false);
				break;
			case COMMENT_PRIVATE_OTHERS:
				view =
				  layoutInflater.inflate(R.layout.item_comment_private_others, viewGroup, false);
				break;
			case COMMENT_PUBLIC_ME:
				view = layoutInflater.inflate(R.layout.item_comment_public_mine, viewGroup, false);
				break;
			case COMMENT_PUBLIC_OTHERS:
				view =
				  layoutInflater.inflate(R.layout.item_comment_public_others, viewGroup, false);
				break;
			case COMMENT_PUBLIC_USER:
				view = layoutInflater.inflate(R.layout.item_comment_public_user, viewGroup, false);
				break;
		}
		return new CommentViewHolder(view, commentClickListener);
	}

	@Override
	protected void bindCustomViewHolder(CommentViewHolder holder, int position, Cursor cursor) {
		holder.clear();
		holder.setSelected(selectedItems.get(holder.getAdapterPosition()));
		long commentId = cursor.getLong(iId);
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
		boolean isTheUser = isPublicChat && alias == null;
		if (isTheUser && userImageUri != null) {
			holder.setUserImageUri(userImageUri);
		}
		if (isTheUser && userName != null) {
			holder.setAnonymousNick(userName);
		}
		holder.setCommentId(commentId);
		long date = cursor.getLong(iDate);
		Long previousDate = null;
		if (!cursor.isLast()) {
			cursor.moveToNext();
			previousDate = cursor.getLong(iDate);
			cursor.moveToPrevious();
		}
		holder.setComment(cursor.getString(iComment), date, previousDate);

		if (chatType == ChatType.FLATTER || chatType == ChatType.FORTHRIGHT) {
			bindVotes(holder, cursor, commentId);
			bindUserVoteType(holder, cursor, commentId);
			bindUserMark(holder, cursor);
			bindCommentFlag(holder, cursor);
		}

		if (iSyncTemp >= 0) {
			holder.setSending(cursor.getInt(iSyncTemp) != 0);
		}
		bindCommentPadding(holder, cursor, anonymousId, alias, isMe, isTheUser);
		bindCommentHeader(holder, cursor, anonymousId, alias, isMe, isTheUser);
	}

	private void bindCommentFlag(CommentViewHolder holder, Cursor cursor) {
		int timesFlagged = cursor.getInt(iTimesFlagged);
		if (timesFlagged > Settings.MIN_FLAG_TO_MARK_COMMENT) {
			int[] flaggedType = new int[FlagReason.values().length];
			flaggedType[FlagReason.ABUSE.ordinal()] = cursor.getInt(iTimesFlaggedAbuse);
			flaggedType[FlagReason.INSULT.ordinal()] = cursor.getInt(iTimesFlaggedInsult);
			flaggedType[FlagReason.LIE.ordinal()] = cursor.getInt(iTimesFlaggedLie);
			flaggedType[FlagReason.OTHER.ordinal()] = cursor.getInt(iTimesFlaggedOther);
			int maxPos = 0;
			int maxValue = flaggedType[0];
			for (int i = 1; i < flaggedType.length; i++) {
				if (maxValue < flaggedType[i]) {
					maxPos = i;
					maxValue = flaggedType[i];
				}
			}
			holder.setCommentFlag(FlagReason.values()[maxPos]);
		} else {
			holder.setCommentFlag(null);
		}
	}

	private void bindUserMark(CommentViewHolder holder, Cursor cursor) {
		UserMark userMark = null;
		String userMarkString = cursor.getString(iUserMark);
		if (userMarkString != null) {
			try {
				userMark = UserMark.valueOf(userMarkString);
			} catch (Exception ignore) {
			}
		}
		holder.setUserMark(userMark);
	}

	private void bindUserVoteType(CommentViewHolder holder, Cursor cursor, long commentId) {
		CommentVoteType userVoteType = null;
		if (isMyPublicChat && commentVoteTypeHashMap.containsKey(commentId)) {
			userVoteType = commentVoteTypeHashMap.get(commentId);
		} else {
			String userVoteTypeString = cursor.getString(iUserVoteType);
			if (userVoteTypeString != null) {
				try {
					userVoteType = CommentVoteType.valueOf(userVoteTypeString);
				} catch (Exception ignore) {
				}
			}
		}
		holder.setUserVoteType(userVoteType, userName);
	}

	private void bindVotes(CommentViewHolder holder, Cursor cursor, long commentId) {
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

	private void bindCommentPadding(CommentViewHolder holder,
									Cursor cursor,
									Long anonymousId,
									String alias,
									boolean isMe,
									boolean isTheUser) {
		if (!cursor.isFirst()) {
			cursor.moveToPrevious();
			boolean previousIsMe = cursor.getInt(iIsMe) != 0;
			Long previousAnonymousId = null;
			if (!cursor.isNull(iUserAnonymousId)) {
				previousAnonymousId = cursor.getLong(iUserAnonymousId);
			}
			String previousAlias = null;
			if (!cursor.isNull(iUserAlias)) {
				previousAlias = cursor.getString(iUserAlias);
			}
			boolean previousIsTheUser = isPublicChat && previousAlias == null;
			if (isTheUser && previousIsTheUser) {
				holder.setExtraPadding(false);
			} else if (isMe && previousIsMe) {
				switch (chatType) {
					case FLATTER:
					case FORTHRIGHT:
						holder.setExtraPadding(!(alias != null && alias.equals(previousAlias)));
						break;
					case PRIVATE:
					case PRIVATE_ANONYMOUS:
					case SECRET:
						holder.setExtraPadding(false);
				}
			} else {
				switch (chatType) {
					case FLATTER:
					case FORTHRIGHT:
						holder.setExtraPadding(!(anonymousId != null &&
												 anonymousId.equals(previousAnonymousId)));
						break;
					case PRIVATE_ANONYMOUS:
					case PRIVATE:
					case SECRET:
						holder.setExtraPadding(isMe || previousIsMe);
						break;
				}
			}
			cursor.moveToNext();
		}
	}

	private void bindCommentHeader(CommentViewHolder holder,
								   Cursor cursor,
								   Long anonymousId,
								   String alias,
								   boolean isMe,
								   boolean isTheUser) {
		if (cursor.isLast()) {
			holder.setFirstCommentOfPerson(true);
		} else {
			cursor.moveToNext();
			boolean nextIsMe = cursor.getInt(iIsMe) != 0;
			Long nextAnonymousId = null;
			if (!cursor.isNull(iUserAnonymousId)) {
				nextAnonymousId = cursor.getLong(iUserAnonymousId);
			}
			String nextAlias = null;
			if (!cursor.isNull(iUserAlias)) {
				nextAlias = cursor.getString(iUserAlias);
			}
			boolean nextIsTheUser = isPublicChat && nextAlias == null;
			if (isTheUser && nextIsTheUser) {
				holder.setExtraPadding(false);
			} else if (isMe && nextIsMe) {
				switch (chatType) {
					case FLATTER:
					case FORTHRIGHT:
						holder.setFirstCommentOfPerson(!(alias != null && alias.equals
																				  (nextAlias)));
						break;
					case PRIVATE:
					case PRIVATE_ANONYMOUS:
					case SECRET:
						holder.setFirstCommentOfPerson(false);
				}
			} else {
				switch (chatType) {
					case FLATTER:
					case FORTHRIGHT:
						holder.setFirstCommentOfPerson(!(anonymousId != null &&
														 anonymousId.equals(nextAnonymousId)));
						break;
					case PRIVATE_ANONYMOUS:
					case PRIVATE:
					case SECRET:
						holder.setFirstCommentOfPerson(isMe || nextIsMe);
						break;
				}
			}
			cursor.moveToPrevious();
		}
	}

	public long getDate(int position) {
		Cursor cursor = getCursor();
		cursor.moveToPosition(position);
		return cursor.getLong(iDate);
	}

	public void toggleSelection(CommentViewHolder holder) {
		int pos = holder.getAdapterPosition();
		if (selectedItems.get(pos, false)) {
			selectedItems.delete(pos);
			holder.setSelected(false);
		} else {
			selectedItems.put(pos, true);
			holder.setSelected(true);
		}
		notifyItemChanged(pos);
	}

	public void clearSelections() {
		selectedItems.clear();
		notifyDataSetChanged();
	}

	public int getSelectedItemCount() {
		return selectedItems.size();
	}

	public List<Integer> getSelectedItems() {
		List<Integer> items = new ArrayList<Integer>(selectedItems.size());
		for (int i = selectedItems.size() - 1; i >= 0; i--) {
			items.add(selectedItems.keyAt(i));
		}
		return items;
	}

	public String getComment(int position) {
		Cursor cursor = getCursor();
		cursor.moveToPosition(position);
		return cursor.getString(iComment);
	}

	public boolean isMe(int position) {
		Cursor cursor = getCursor();
		cursor.moveToPosition(position);
		return cursor.getInt(iIsMe) != 0;
	}

	public CommentVoteType getVote(int position) {
		CommentVoteType voteType = null;
		Cursor cursor = getCursor();
		cursor.moveToPosition(position);
		Long commentId = cursor.getLong(iId);
		if (commentVoteTypeHashMap.containsKey(commentId)) {
			voteType = commentVoteTypeHashMap.get(commentId);
		} else {
			String voteTypeString = cursor.getString(iVoteType);
			if (voteTypeString != null) {
				try {
					voteType = CommentVoteType.valueOf(voteTypeString);
				} catch (Exception ignore) {
				}
			}
		}
		return voteType;
	}
}
