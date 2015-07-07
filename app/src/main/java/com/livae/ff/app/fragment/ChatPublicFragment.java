package com.livae.ff.app.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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
import com.livae.ff.app.dialog.EditTextDialogFragment;
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
import com.livae.ff.app.task.TextId;
import com.livae.ff.app.utils.AnimUtils;
import com.livae.ff.app.viewholders.CommentViewHolder;
import com.livae.ff.common.Constants.ChatType;
import com.livae.ff.common.Constants.CommentVoteType;

public class ChatPublicFragment extends AbstractLoaderFragment<CommentViewHolder, QueryId>
  implements CommentActionListener, View.OnClickListener {

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

	private MenuItem editMenuItem;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras.containsKey(AbstractChatActivity.EXTRA_CONVERSATION_ID)) {
			conversationId = extras.getLong(AbstractChatActivity.EXTRA_CONVERSATION_ID);
		}
		chatType = (ChatType) extras.getSerializable(AbstractChatActivity.EXTRA_CHAT_TYPE);
		anonymousNick = extras.getString(AbstractChatActivity.EXTRA_ROOM_NAME, null);
		conversationPhone = extras.getLong(AbstractChatActivity.EXTRA_PHONE_NUMBER);
		isMyPublicChat = Application.appUser().getUserPhone().equals(conversationPhone);
		super.onCreate(savedInstanceState);

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
		view.findViewById(R.id.center_progressbar).setVisibility(View.VISIBLE);
		commentPostContainer = view.findViewById(R.id.comment_post);
		buttonPostComment = (FloatingActionButton) view.findViewById(R.id.button_post_comment);
		commentText = (EditText) view.findViewById(R.id.comment_text);
		commentPostContainer.setVisibility(View.GONE);
		buttonPostComment.setVisibility(View.GONE);
		buttonPostComment.setOnClickListener(this);
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
		if (conversationId == null) {
			getConversation();
		} else {
			startConversation();
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
		if (conversationId != null) {
			leaveConversation();
		}
	}

	@Override
	protected NetworkAsyncTask<QueryId, ListResult> getLoaderTask() {
		return new TaskCommentsGet();
	}

	@Override
	protected Uri getUriCursor() {
		return ConversationsProvider.getUriConversationComments(conversationId);
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		switch (id) {
			case LOADER_ID:
				return new CursorLoader(getActivity(), getUriCursor(), getProjection(), null, null,
										Integer.toString(getTotalLoaded()));
			// break
			case LOADER_CONVERSATION_ID:
				return new CursorLoader(getActivity(),
										ConversationsProvider.getUriConversation(conversationId),
										null, null, null, null);
			// break
		}
		return super.onCreateLoader(id, bundle);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> objectLoader, Cursor cursor) {
		switch (objectLoader.getId()) {
			case LOADER_CONVERSATION_ID:
				if (cursor.moveToFirst()) {
					final AbstractChatActivity activity = (AbstractChatActivity) getActivity();
					final int iRoomName = cursor.getColumnIndex(Table.Conversation.ROOM_NAME);
					anonymousNick = cursor.getString(iRoomName);
					final int iDisplayName = cursor.getColumnIndex(Table.LocalUser.CONTACT_NAME);
					final String displayName = cursor.getString(iDisplayName);
					final int iImageUri = cursor.getColumnIndex(Table.LocalUser.IMAGE_URI);
					final String imageUri = cursor.getString(iImageUri);
					final int iPhoneNumber = cursor.getColumnIndex(Table.Conversation.PHONE);
					conversationPhone = cursor.getLong(iPhoneNumber);
					activity.bindToolbar(anonymousNick, displayName, imageUri, null,
										 conversationPhone);
					if (!isMyPublicChat) {
						if (anonymousNick == null) {
							Handler handler = new Handler();
							handler.post(new Runnable() {
								@Override
								public void run() {
									requestNickName(false);
								}
							});
						} else {
							showSendMessagesPanel();
						}
					}
				}
				break;
			default:
				super.onLoadFinished(objectLoader, cursor);
		}
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

	private void startConversation() {
		getLoaderManager().restartLoader(LOADER_CONVERSATION_ID, Bundle.EMPTY,
										 ChatPublicFragment.this);
		registerContentObserver();
		joinConversation();
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
			requestNickName(true);
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
		new TaskConversationLeave().execute(conversationId, null);
	}

	private void requestNickName(boolean cancelable) {
		EditTextDialogFragment dialog = new EditTextDialogFragment() {

			@Override
			protected void performAction(EditTextDialogFragment dialog, String newText) {
				anonymousNick = newText;
				ContentResolver contentResolver = getActivity().getContentResolver();
				ContentValues values = new ContentValues();
				values.put(Table.Conversation.ROOM_NAME, anonymousNick);
				contentResolver.update(ConversationsProvider.getUriConversation(conversationId),
									   values, null, null);
				Application.appUser().getChats().setUserAnonymousName(anonymousNick);
				dialog.dismiss();
				showSendMessagesPanel();
				final AbstractChatActivity activity = (AbstractChatActivity) getActivity();
				activity.bindToolbar(anonymousNick, null, null, null, conversationPhone);
			}
		};
		dialog.setCancelable(cancelable);
		dialog.show(getActivity(), getActivity().getSupportFragmentManager(),
					R.string.anonymous_name_title, R.string.anonymous_name_message,
					R.integer.anonymous_name_max_chars,
					Application.appUser().getChats().getUserAnonymousName());
	}

	private void showSendMessagesPanel() {
		if (editMenuItem != null) {
			editMenuItem.setVisible(true);
		}
		if (commentPostContainer.getVisibility() != View.VISIBLE) {
			Resources res = getResources();
			int height = commentPostContainer.getHeight();
			int margin = res.getDimensionPixelSize(R.dimen.space_normal) * 2;
			AnimUtils.build(commentPostContainer).alpha(0.2f, 1).translateY(height + margin, 0)
					 .accelerateDecelerate().start();
			height = buttonPostComment.getHeight();
			AnimUtils.build(buttonPostComment).alpha(0.2f, 1).translateY(height + margin, 0)
					 .accelerateDecelerate().start();
			commentPostContainer.setVisibility(View.VISIBLE);
			buttonPostComment.setVisibility(View.VISIBLE);
		}
	}

	private void postComment(String comment) {
		if (taskPostComment == null) {
			taskPostComment = new TaskPostComment();
		}
		TextId textId = new TextId(comment, conversationId, anonymousNick);
		commentText.setEnabled(false);
		taskPostComment.execute(textId, new Callback<TextId, Comment>() {
			@Override
			public void onComplete(CustomAsyncTask<TextId, Comment> task, TextId param,
								   Comment result) {
				switch (chatType) {
					case FLATTER:
						Analytics.event(Analytics.Category.CONTENT,
										Analytics.Action.COMMENT_FLATTER);
						break;
					case FORTHRIGHT:
						Analytics.event(Analytics.Category.CONTENT,
										Analytics.Action.COMMENT_FORTHRIGHT);
						break;
				}
				if (isResumed()) {
					commentText.setEnabled(true);
					commentText.setText("");
					commentText.clearFocus();
					reloadCursor();
					InputMethodManager imm;
					imm = (InputMethodManager) getActivity()
												 .getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(commentText.getWindowToken(), 0);
				}
			}

			@Override
			public void onError(CustomAsyncTask<TextId, Comment> task, TextId param, Exception e) {
				if (isResumed()) {
					commentText.setEnabled(true);
					AbstractActivity activity = (AbstractActivity) getActivity();
					if (e instanceof GoogleJsonResponseException) {
						GoogleJsonResponseException ge = (GoogleJsonResponseException) e;
						if (ge.getDetails() != null) {
							activity.showSnackBarException(e);
						} else {
							activity.showSnackBarException(getString(R.string.error_unknown,
																	 e.getMessage()));
						}
					} else {
						activity.showSnackBarException(e);
					}
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_post_comment:
				String comment = commentText.getText().toString().trim();
				int length = comment.length();
				if (length > 0) {
					postComment(comment);
				} else {
					commentText.setText(comment);
				}
				break;
		}
	}
}
