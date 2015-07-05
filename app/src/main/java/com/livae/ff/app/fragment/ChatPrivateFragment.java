package com.livae.ff.app.fragment;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Pair;
import android.widget.EditText;

import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.Application;
import com.livae.ff.app.activity.AbstractActivity;
import com.livae.ff.app.adapter.CommentsAdapter;
import com.livae.ff.app.adapter.EndlessCursorAdapter;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.CommentActionListener;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.task.ListResult;
import com.livae.ff.app.task.QueryId;
import com.livae.ff.app.task.TaskCommentVoteAgree;
import com.livae.ff.app.task.TaskCommentVoteDelete;
import com.livae.ff.app.task.TaskCommentVoteDisagree;
import com.livae.ff.app.task.TaskCommentsGet;
import com.livae.ff.app.task.TaskPostComment;
import com.livae.ff.app.viewholders.CommentViewHolder;
import com.livae.ff.common.Constants.ChatType;
import com.livae.ff.common.Constants.CommentVoteType;

public class ChatPrivateFragment extends AbstractLoaderFragment<CommentViewHolder, QueryId>
  implements CommentActionListener {

	private TaskCommentVoteAgree taskVoteAgreeComment;

	private TaskCommentVoteDisagree taskVoteDisagreeComment;

	private TaskCommentVoteDelete taskNoVoteComment;

	private TaskPostComment taskPostComment;

	private FloatingActionButton buttonPostComment;

	private EditText commentText;

	private Long conversationId;

	private ChatType conversationType;

	private String anonymousNick;

	private Long conversationPhone;

	private boolean isMyPublicChat;

	private CommentsAdapter commentsAdapter;

	private ContentObserver contentObserver = new ContentObserver(null) {

		@Override
		public void onChange(boolean selfChange) {
			// TODO
//			getLoaderManager().restartLoader(LOAD_CHATS, Bundle.EMPTY, ChatsFragment.this);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isMyPublicChat = false;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (taskVoteAgreeComment != null) {
			taskVoteAgreeComment.cancel();
		}
		if (taskVoteDisagreeComment != null) {
			taskVoteDisagreeComment.cancel();
		}
		if (taskNoVoteComment != null) {
			taskNoVoteComment.cancel();
		}
		final ContentResolver cr = getActivity().getContentResolver();
		cr.unregisterContentObserver(contentObserver);
	}

	@Override
	protected NetworkAsyncTask<QueryId, ListResult> getLoaderTask() {
		return new TaskCommentsGet();
	}

	@Override
	protected Uri getUriCursor() {
		return null;
	}

	@Override
	protected EndlessCursorAdapter<CommentViewHolder> getAdapter() {
		commentsAdapter = new CommentsAdapter(this, this, conversationType);
		return commentsAdapter;
	}

	@Override
	protected String[] getProjection() {
		return CommentsAdapter.PROJECTION;
	}

	@Override
	protected QueryId getBaseQueryParams() {
		return new QueryId(conversationId);
	}

	@Override
	protected String getOrderString() {
		return Table.Comment.DATE;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (conversationId != null) {
			final ContentResolver cr = getActivity().getContentResolver();
			cr.registerContentObserver(ConversationsProvider.getUriConversation(conversationId),
									   true, contentObserver);
		}
	}

	@Override
	public void commentVotedAgree(Long commentId, Long userCommentId, int adapterPosition) {
		if (taskVoteAgreeComment == null) {
			taskVoteAgreeComment = new TaskCommentVoteAgree();
		}
		Pair<Long, Integer> param = new Pair<>(commentId, adapterPosition);
		taskVoteAgreeComment.execute(param, new Callback<Pair<Long, Integer>, Comment>() {
			@Override
			public void onComplete(CustomAsyncTask<Pair<Long, Integer>, Comment> task,
								   Pair<Long, Integer> param, Comment result) {
				Analytics.event(Analytics.Category.CONTENT, Analytics.Action.COMMENT_VOTED_AGREE);
				commentsAdapter.votedComment(param.first, CommentVoteType.AGREE);
				if (!task.isCancelled()) {
					commentsAdapter.notifyItemChanged(param.second);
				}
			}

			@Override
			public void onError(CustomAsyncTask<Pair<Long, Integer>, Comment> task,
								Pair<Long, Integer> param, Exception e) {
				if (!task.isCancelled()) {
					AbstractActivity activity = (AbstractActivity) getActivity();
					activity.showSnackBarException(e);
				}
			}
		});
	}

	@Override
	public void commentVotedDisagree(Long commentId, Long userCommentId, int adapterPosition) {
		if (taskVoteDisagreeComment == null) {
			taskVoteDisagreeComment = new TaskCommentVoteDisagree();
		}
		Pair<Long, Integer> param = new Pair<>(commentId, adapterPosition);
		taskVoteDisagreeComment.execute(param, new Callback<Pair<Long, Integer>, Comment>() {
			@Override
			public void onComplete(CustomAsyncTask<Pair<Long, Integer>, Comment> task,
								   Pair<Long, Integer> param, Comment result) {
				Analytics.event(Analytics.Category.CONTENT,
								Analytics.Action.COMMENT_VOTED_DISAGREE);
				commentsAdapter.votedComment(param.first, CommentVoteType.DISAGREE);
				if (!task.isCancelled()) {
					commentsAdapter.notifyItemChanged(param.second);
				}
			}

			@Override
			public void onError(CustomAsyncTask<Pair<Long, Integer>, Comment> task,
								Pair<Long, Integer> param, Exception e) {
				if (!task.isCancelled()) {
					AbstractActivity activity = (AbstractActivity) getActivity();
					activity.showSnackBarException(e);
				}
			}
		});
	}

	@Override
	public void commentNoVoted(Long commentId, Long userCommentId, int adapterPosition) {
		if (taskNoVoteComment == null) {
			taskNoVoteComment = new TaskCommentVoteDelete();
		}
		Pair<Long, Integer> param = new Pair<>(commentId, adapterPosition);
		taskNoVoteComment.execute(param, new Callback<Pair<Long, Integer>, Comment>() {
			@Override
			public void onComplete(CustomAsyncTask<Pair<Long, Integer>, Comment> task,
								   Pair<Long, Integer> param, Comment result) {
				Analytics.event(Analytics.Category.CONTENT, Analytics.Action.COMMENT_VOTE_REMOVED);
				commentsAdapter.votedComment(param.first, null);
				if (!task.isCancelled()) {
					commentsAdapter.notifyItemChanged(param.second);
				}
			}

			@Override
			public void onError(CustomAsyncTask<Pair<Long, Integer>, Comment> task,
								Pair<Long, Integer> param, Exception e) {
				if (!task.isCancelled()) {
					AbstractActivity activity = (AbstractActivity) getActivity();
					activity.showSnackBarException(e);
				}
			}
		});
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
		final ContentResolver cr = getActivity().getContentResolver();
		if (conversationId != null) {
			cr.registerContentObserver(ConversationsProvider.getUriConversation(conversationId),
									   true, contentObserver);
			restart();
		} else {
			cr.unregisterContentObserver(contentObserver);
		}
	}

	public void setConversationType(ChatType conversationType) {
		this.conversationType = conversationType;
		checkCanSendMessages();
	}

	public void setAnonymousNick(String anonymousNick) {
		this.anonymousNick = anonymousNick;
		checkCanSendMessages();
	}

	public void setConversationPhone(Long conversationPhone) {
		this.conversationPhone = conversationPhone;
		checkCanSendMessages();
	}

	private void checkCanSendMessages() {
		boolean canSendMessages = true;
		if (conversationType != null) {
			switch (conversationType) {
				case FLATTER:
				case FORTHRIGHT:
					if (conversationPhone != null && anonymousNick != null) {
						isMyPublicChat = Application.appUser().getUserPhone()
													.equals(conversationPhone);
						canSendMessages = !isMyPublicChat;
					}
					break;
				case PRIVATE:
				case SECRET:
				case PRIVATE_ANONYMOUS:
					canSendMessages = true;
					break;
			}
		}
		if (canSendMessages) {
			// TODO now can send messages
		}
	}
}
