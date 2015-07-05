package com.livae.ff.app.fragment;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.api.ff.model.Conversation;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.activity.AbstractActivity;
import com.livae.ff.app.activity.AbstractChatActivity;
import com.livae.ff.app.adapter.CommentsAdapter;
import com.livae.ff.app.adapter.EndlessCursorAdapter;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.CommentActionListener;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.task.ConversationParams;
import com.livae.ff.app.task.ListResult;
import com.livae.ff.app.task.QueryId;
import com.livae.ff.app.task.TaskCommentVoteAgree;
import com.livae.ff.app.task.TaskCommentVoteDelete;
import com.livae.ff.app.task.TaskCommentVoteDisagree;
import com.livae.ff.app.task.TaskCommentsGet;
import com.livae.ff.app.task.TaskConversationCreate;
import com.livae.ff.app.task.TaskConversationJoin;
import com.livae.ff.app.task.TaskConversationLeave;
import com.livae.ff.app.task.TaskPostComment;
import com.livae.ff.app.utils.AnimUtils;
import com.livae.ff.app.viewholders.CommentViewHolder;
import com.livae.ff.common.Constants.ChatType;
import com.livae.ff.common.Constants.CommentVoteType;

public class ChatPublicFragment extends AbstractLoaderFragment<CommentViewHolder, QueryId>
  implements CommentActionListener {

	private static final int LOADER_CONVERSATION_ID = 2;

	private TaskCommentVoteAgree taskVoteAgreeComment;

	private TaskCommentVoteDisagree taskVoteDisagreeComment;

	private TaskCommentVoteDelete taskNoVoteComment;

	private TaskPostComment taskPostComment;

	private View commentPostContainer;

	private FloatingActionButton buttonPostComment;

	private EditText commentText;

	private Long conversationId;

	private Long conversationPhone;

	private String anonymousNick;

	private ChatType chatType;

	private boolean isMyPublicChat;

	private CommentsAdapter commentsAdapter;

	private ContentObserver contentObserver = new ContentObserver(null) {

		@Override
		public void onChange(boolean selfChange) {
			reloadCursor();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isMyPublicChat = false;
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras.containsKey(AbstractChatActivity.EXTRA_PHONE_NUMBER)) {
			conversationId = extras.getLong(AbstractChatActivity.EXTRA_PHONE_NUMBER);
		}
		chatType = (ChatType) extras.getSerializable(AbstractChatActivity.EXTRA_CHAT_TYPE);
		anonymousNick = extras.getString(AbstractChatActivity.EXTRA_ROOM_NAME, null);
		conversationPhone = extras.getLong(AbstractChatActivity.EXTRA_PHONE_NUMBER);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_comments, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		commentPostContainer = view.findViewById(R.id.comment_post_container);
		buttonPostComment = (FloatingActionButton) view.findViewById(R.id.button_post_comment);
		commentText = (EditText) view.findViewById(R.id.comment_text);
		commentPostContainer.setVisibility(View.GONE);
	}

	@Override
	public void onResume() {
		super.onResume();
		checkCanSendMessages();
		if (conversationId == null) {
			getConversation();
		} else {
			getLoaderManager().restartLoader(LOADER_CONVERSATION_ID, Bundle.EMPTY,
											 ChatPublicFragment.this);
			registerContentObserver();
			joinConversation();
		}
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
		leaveConversation();
	}

	@Override
	protected NetworkAsyncTask<QueryId, ListResult> getLoaderTask() {
		return new TaskCommentsGet();
	}

	@Override
	protected Uri getUriCursor() {
		return ConversationsProvider.getUriConversation(conversationId);
	}

	@Override
	protected EndlessCursorAdapter<CommentViewHolder> getAdapter() {
		commentsAdapter = new CommentsAdapter(this, this, chatType);
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

	private void getConversation() {
		ConversationParams conversationParams = new ConversationParams(chatType, conversationPhone);
		Callback<ConversationParams, Conversation> callback;
		callback = new Callback<ConversationParams, Conversation>() {
			@Override
			public void onComplete(CustomAsyncTask<ConversationParams, Conversation> task,
								   ConversationParams conversationParams,
								   Conversation conversation) {
				conversationId = conversation.getId();
				if (isResumed()) {
					getLoaderManager().restartLoader(LOADER_CONVERSATION_ID, Bundle.EMPTY,
													 ChatPublicFragment.this);
				}
			}

			@Override
			public void onError(CustomAsyncTask<ConversationParams, Conversation> task,
								ConversationParams conversationParams, Exception e) {
				if (isResumed()) {
					AbstractActivity abstractActivity = (AbstractActivity) getActivity();
					abstractActivity.showSnackBarException(e);
				}
			}
		};
		new TaskConversationCreate().execute(conversationParams, callback);
	}

	private void registerContentObserver() {
		if (conversationId != null) {
			final ContentResolver cr = getActivity().getContentResolver();
			cr.registerContentObserver(ConversationsProvider.getUriConversation(conversationId),
									   true, contentObserver);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.edit_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_edit) {
			// TODO
			return true;
		}
		return super.onOptionsItemSelected(item);
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

	private void checkCanSendMessages() {
		boolean canSendMessages = true;
		if (chatType != null) {
			switch (chatType) {
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
			Resources res = getResources();
			int shortTime = res.getInteger(android.R.integer.config_shortAnimTime);
			int height = commentPostContainer.getHeight();
			int margin = res.getDimensionPixelSize(R.dimen.space_normal);
			AnimUtils.build(commentPostContainer).alpha(0.2f, 1).translateY(height + margin, 0)
					 .accelerateDecelerate().start();
			height = buttonPostComment.getHeight();
			AnimUtils.build(buttonPostComment).alpha(0.2f, 1).translateY(height + margin, 0)
					 .accelerateDecelerate().start();
			commentPostContainer.setVisibility(View.VISIBLE);
		}
	}

	private void joinConversation() {
		new TaskConversationJoin().execute(conversationId, new Callback<Long, Void>() {
			@Override
			public void onComplete(CustomAsyncTask<Long, Void> task, Long aLong, Void aVoid) {
				startLoading();
			}

			@Override
			public void onError(CustomAsyncTask<Long, Void> task, Long aLong, Exception e) {
				if (isResumed()) {
					AbstractActivity activity = (AbstractActivity) getActivity();
					activity.showSnackBarException(e);
				}
			}
		});
	}

	private void leaveConversation() {
		new TaskConversationLeave().execute(conversationId, new Callback<Long, Void>() {
			@Override
			public void onComplete(CustomAsyncTask<Long, Void> task, Long aLong, Void aVoid) {
				// nothing
			}

			@Override
			public void onError(CustomAsyncTask<Long, Void> task, Long aLong, Exception e) {
				// nothing
			}
		});
	}
}
