package com.livae.ff.app.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.api.ff.model.Conversation;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.Application;
import com.livae.ff.app.R;
import com.livae.ff.app.activity.AbstractActivity;
import com.livae.ff.app.activity.AbstractChatActivity;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.dialog.EditTextDialogFragment;
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
import com.livae.ff.common.Constants.CommentVoteType;

public class ChatPublicFragment extends AbstractChatFragment {

	private TaskCommentVoteAgree taskVoteAgreeComment;

	private TaskCommentVoteDisagree taskVoteDisagreeComment;

	private TaskCommentVoteDelete taskNoVoteComment;

	private boolean isMyPublicChat;

	private MenuItem editMenuItem;

	private EditTextDialogFragment dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isMyPublicChat = Application.appUser().getUserPhone().equals(conversationPhone);
		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		switch (chatType) {
			case FLATTER:
				setEmptyViewText(R.string.empty_chat_flatter);
				break;
			case FORTHRIGHT:
				setEmptyViewText(R.string.empty_chat_forthright);
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
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
	}

	@Override
	public void onLoadFinished(Loader<Cursor> objectLoader, Cursor cursor) {
		super.onLoadFinished(objectLoader, cursor);
		switch (objectLoader.getId()) {
			case LOADER_CONVERSATION_ID:
				if (!isMyPublicChat) {
					if (anonymousNick == null) {
						Handler handler = new Handler();
						handler.post(new Runnable() {
							@Override
							public void run() {
								requestNickName();
							}
						});
					} else {
						showSendMessagesPanel();
					}
				} else {
					showSendMessagesPanel();
					switch (chatType) {
						case FLATTER:
							Application.appUser().getChats().setChatFlatterUnread(0);
							break;
						case FORTHRIGHT:
							Application.appUser().getChats().setChatForthrightUnread(0);
							break;
					}
				}
				break;
			default:
				break;
		}
	}

	@Override
	protected void getConversation() {
		ConversationParams conversationParams = new ConversationParams(chatType, conversationPhone);
		Callback<ConversationParams, Conversation> callback;
		callback = new Callback<ConversationParams, Conversation>() {
			@Override
			public void onComplete(CustomAsyncTask<ConversationParams, Conversation> task,
								   ConversationParams conversationParams,
								   Conversation conversation) {
				conversationId = conversation.getId();
				if (isResumed()) {
					startConversation();
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

	@Override
	protected void startConversation() {
		super.startConversation();
		restart();
	}

	protected void showSendMessagesPanel() {
		if (editMenuItem != null) {
			editMenuItem.setVisible(true);
		}
		super.showSendMessagesPanel();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.edit_menu, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		editMenuItem = menu.findItem(R.id.action_edit);
		if (anonymousNick == null) {
			editMenuItem.setVisible(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_edit) {
			requestNickName();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected NetworkAsyncTask<QueryId, ListResult> getLoaderTask() {
		return new TaskCommentsGet();
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

	private synchronized void requestNickName() {
		if (dialog == null) {
			dialog = new EditTextDialogFragment() {

				@Override
				protected void performAction(EditTextDialogFragment dialogFragment,
											 String newText) {
					anonymousNick = newText;
					ContentResolver contentResolver = getActivity().getContentResolver();
					ContentValues values = new ContentValues();
					values.put(Table.Conversation.ROOM_NAME, anonymousNick);
					contentResolver.update(ConversationsProvider.getUriConversation(conversationId),
										   values, null, null);
					Application.appUser().getChats().setUserAnonymousName(anonymousNick);
					dialogFragment.dismiss();
					showSendMessagesPanel();
					final AbstractChatActivity activity = (AbstractChatActivity) getActivity();
					activity.bindToolbar(anonymousNick, null, null, null, conversationPhone);
					dialog = null;
				}

				@Override
				public void onCancel(DialogInterface dialogInterface) {
					if (anonymousNick == null) {
						dialog = null;
						getActivity().finish();
					}
				}
			};
			dialog.setCancelable(true);
			dialog.show(getActivity(), getActivity().getSupportFragmentManager(),
						R.string.anonymous_name_title, R.string.anonymous_name_message,
						R.integer.anonymous_name_max_chars,
						Application.appUser().getChats().getUserAnonymousName());
		}
	}

}
