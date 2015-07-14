package com.livae.ff.app.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.R;
import com.livae.ff.app.activity.AbstractActivity;
import com.livae.ff.app.activity.AbstractChatActivity;
import com.livae.ff.app.adapter.CommentsAdapter;
import com.livae.ff.app.adapter.EndlessCursorAdapter;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.listener.CommentActionListener;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.receiver.NotificationDisabledReceiver;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.task.QueryId;
import com.livae.ff.app.task.TaskConversationJoin;
import com.livae.ff.app.task.TaskConversationLeave;
import com.livae.ff.app.task.TaskPostComment;
import com.livae.ff.app.task.TextId;
import com.livae.ff.app.utils.AnimUtils;
import com.livae.ff.app.viewholders.CommentViewHolder;
import com.livae.ff.common.Constants.ChatType;
import com.livae.ff.common.model.Notification;
import com.livae.ff.common.model.NotificationComment;

public abstract class AbstractChatFragment
  extends AbstractLoaderFragment<CommentViewHolder, QueryId>
  implements CommentActionListener, View.OnClickListener,
			 NotificationDisabledReceiver.CloudMessagesDisabledListener {

	protected static final int LOADER_CONVERSATION_ID = 2;

	protected Long conversationId;

	protected Long conversationPhone;

	protected String anonymousNick;

	protected ChatType chatType;

	protected CommentsAdapter commentsAdapter;

	protected String userImageUri;

	protected String userName;

	protected Long lastAccess;

	protected Long lastMessage;

	private FloatingActionButton buttonPostComment;

	private EditText commentText;

	private View commentPostContainer;

	private TaskPostComment taskPostComment;

	private ContentObserver contentObserver;

	private NotificationDisabledReceiver notificationDisabledReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Bundle extras = getActivity().getIntent().getExtras();
		if (extras.containsKey(AbstractChatActivity.EXTRA_CONVERSATION_ID)) {
			conversationId = extras.getLong(AbstractChatActivity.EXTRA_CONVERSATION_ID);
		}
		chatType = (ChatType) extras.getSerializable(AbstractChatActivity.EXTRA_CHAT_TYPE);
		anonymousNick = extras.getString(AbstractChatActivity.EXTRA_ROOM_NAME, null);
		conversationPhone = extras.getLong(AbstractChatActivity.EXTRA_PHONE_NUMBER);
		userImageUri = extras.getString(AbstractChatActivity.EXTRA_IMAGE_URI);
		userName = extras.getString(AbstractChatActivity.EXTRA_DISPLAY_NAME);
		if (extras.containsKey(AbstractChatActivity.EXTRA_LAST_ACCESS_DATE)) {
			lastAccess = extras.getLong(AbstractChatActivity.EXTRA_LAST_ACCESS_DATE);
		}
		if (extras.containsKey(AbstractChatActivity.EXTRA_LAST_MESSAGE_DATE)) {
			lastMessage = extras.getLong(AbstractChatActivity.EXTRA_LAST_MESSAGE_DATE);
		}
		super.onCreate(savedInstanceState);

		contentObserver = new ContentObserver(new Handler()) {

			@Override
			public void onChange(boolean selfChange) {
				reloadCursor();
			}
		};
		notificationDisabledReceiver = new NotificationDisabledReceiver();
		notificationDisabledReceiver.setListener(this);

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
		buttonPostComment = (FloatingActionButton) view.findViewById(R.id.button_post_comment);
		commentText = (EditText) view.findViewById(R.id.comment_text);
		commentPostContainer = view.findViewById(R.id.comment_post);
		buttonPostComment.setOnClickListener(this);
		commentPostContainer.setVisibility(View.GONE);
		buttonPostComment.setVisibility(View.GONE);
	}

	@Override
	public void onResume() {
		super.onResume();
		notificationDisabledReceiver.register(getActivity());
		if (conversationId == null) {
			getConversation();
		} else {
			startConversation();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		notificationDisabledReceiver.unregister(getActivity());
		final ContentResolver cr = getActivity().getContentResolver();
		cr.unregisterContentObserver(contentObserver);
		if (conversationId != null) {
			leaveConversation();
		}
	}

	@Override
	protected Uri getUriCursor() {
		return ConversationsProvider.getUriConversationComments(conversationId);
	}

	@Override
	protected EndlessCursorAdapter<CommentViewHolder> getAdapter() {
		commentsAdapter = new CommentsAdapter(this, this, this, chatType, userName, userImageUri);
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
		return "-" + Table.Comment.DATE;
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
					String imageUri = null;
					if (!cursor.isNull(iImageUri)) {
						imageUri = cursor.getString(iImageUri);
					}
					final int iAnonymousId = cursor.getColumnIndex(Table.Conversation.ALIAS_ID);
					Long anonymousId = null;
					if (!cursor.isNull(iAnonymousId)) {
						anonymousId = cursor.getLong(iAnonymousId);
					}
					final int iPhoneNumber = cursor.getColumnIndex(Table.Conversation.PHONE);
					if (!cursor.isNull(iPhoneNumber)) {
						conversationPhone = cursor.getLong(iPhoneNumber);
					}
					activity.bindToolbar(anonymousNick, displayName, imageUri, anonymousId,
										 conversationPhone);

					final ContentResolver contentResolver = activity.getContentResolver();
					final Uri uriConversation = ConversationsProvider
												  .getUriConversation(conversationId);
					final ContentValues contentValues = new ContentValues();
					contentValues.put(Table.Conversation.LAST_ACCESS, System.currentTimeMillis());
					contentResolver.update(uriConversation, contentValues, null, null);
				}
				break;
			default:
				super.onLoadFinished(objectLoader, cursor);
				break;
		}
	}

	abstract protected void getConversation();

	protected void startConversation() {
		getLoaderManager().restartLoader(LOADER_CONVERSATION_ID, Bundle.EMPTY,
										 AbstractChatFragment.this);
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
		ContentValues values = new ContentValues();
		values.put(Table.Conversation.UNREAD, 0);
		Uri uri = ConversationsProvider.getUriConversation(conversationId);
		getActivity().getContentResolver().update(uri, values, null, null);
	}

	private void leaveConversation() {
		new TaskConversationLeave().execute(conversationId, null);
	}

	private void postComment(String comment) {
		if (taskPostComment == null) {
			taskPostComment = new TaskPostComment();
		}
		TextId textId = new TextId(comment, conversationId, anonymousNick);
		buttonPostComment.setEnabled(false);
		scrollToPosition(0, true);
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
					case PRIVATE_ANONYMOUS:
						Analytics.event(Analytics.Category.CONTENT,
										Analytics.Action.COMMENT_ANONYMOUS);
						break;
					case PRIVATE:
						Analytics.event(Analytics.Category.CONTENT,
										Analytics.Action.COMMENT_PRIVATE);
						break;
					case SECRET:
						Analytics.event(Analytics.Category.CONTENT,
										Analytics.Action.COMMENT_SECRET);
						break;
				}
				increaseTotalLoaded();
				reloadCursor();
				if (isResumed()) {
					commentText.setEnabled(true);
					buttonPostComment.setEnabled(true);
					commentText.setText("");
				}
			}

			@Override
			public void onError(CustomAsyncTask<TextId, Comment> task, TextId param, Exception e) {
				if (isResumed()) {
					commentText.setEnabled(true);
					buttonPostComment.setEnabled(true);
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

	@Override
	public boolean onNotificationReceived(Notification notification) {
		if (notification instanceof NotificationComment) {
			NotificationComment nc = (NotificationComment) notification;
			if (conversationId != null && conversationId.equals(nc.getConversationId())) {
				if (!nc.getIsMe()) {
					increaseTotalLoaded();
					reloadCursor();
				}
				return true;
			}
		}
		return false;
	}

	protected void showSendMessagesPanel() {
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
}