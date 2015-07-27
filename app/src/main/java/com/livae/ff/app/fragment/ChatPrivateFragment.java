package com.livae.ff.app.fragment;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.livae.ff.api.ff.model.Conversation;
import com.livae.ff.app.R;
import com.livae.ff.app.activity.AbstractActivity;
import com.livae.ff.app.activity.AbstractChatActivity;
import com.livae.ff.app.activity.ChatPrivateActivity;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.dialog.EditTextDialogFragment;
import com.livae.ff.app.task.ConversationParams;
import com.livae.ff.app.task.ListResult;
import com.livae.ff.app.task.QueryId;
import com.livae.ff.app.task.TaskConversationCreate;
import com.livae.ff.common.Constants;
import com.livae.ff.common.Constants.ChatType;

public class ChatPrivateFragment extends AbstractChatFragment {

	private boolean endPreviousMessages;

	private EditTextDialogFragment dialogRoomName;

	private boolean firstTimeLoad;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		firstTimeLoad = savedInstanceState == null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> objectLoader, Cursor cursor) {
		super.onLoadFinished(objectLoader, cursor);
		switch (objectLoader.getId()) {
			case LOADER_ID:
				if (cursor.getCount() < getTotalLoaded()) {
					endPreviousMessages = true;
				}
				if (firstTimeLoad && unreadMessages != null) {
					firstTimeLoad = false;
					if (unreadMessages > 0) {
						scrollToPosition(unreadMessages - 1, false);
					}
				}
				break;
			default:
				break;
		}
	}

	@Override
	protected void getConversation() {
		switch (chatType) {
			case PRIVATE:
			case SECRET:
				startChat(chatType, conversationPhone, null);
				break;
			case PRIVATE_ANONYMOUS:
				requestRoomName();
				break;
		}
	}

	@Override
	protected void startConversation() {
		super.startConversation();
		showSendMessagesPanel();
		if (chatType == ChatType.SECRET) {
			ChatPrivateActivity activity = (ChatPrivateActivity) getActivity();
			activity.setSecretConversationId(conversationId);
		}
	}

	@Override
	protected CustomAsyncTask<QueryId, ListResult> getLoaderTask() {
		endPreviousMessages = false;
		return new CustomAsyncTask<QueryId, ListResult>() {

			@Override
			public CustomAsyncTask<QueryId, ListResult> execute(QueryId queryId,
																Callback<QueryId, ListResult> callback) {
				callback.onComplete(this, queryId, doInBackground(queryId));
				return this;
			}

			@Override
			protected ListResult doInBackground(QueryId queryId) {
				int toLoad = 100;
				if (endPreviousMessages) {
					toLoad = 0;
				} else if (firstTimeLoad) {
					toLoad = Math.max(unreadMessages + 20, toLoad);
				}
				return new ListResult(endPreviousMessages ? null : "", toLoad);
			}
		};
	}

	private synchronized void requestRoomName() {
		if (dialogRoomName == null) {
			dialogRoomName = new EditTextDialogFragment() {

				@Override
				protected void performAction(EditTextDialogFragment dialogFragment,
											 String newText) {
					anonymousNick = newText;
					dialogFragment.dismiss();
					startChat(chatType, conversationPhone, anonymousNick);

					final AbstractChatActivity activity = (AbstractChatActivity) getActivity();
					activity.bindToolbar(anonymousNick, userName, userImageUri, null,
										 conversationPhone);
				}

				@Override
				public void onCancel(DialogInterface dialogInterface) {
					if (anonymousNick == null) {
						dialogRoomName = null;
						getActivity().finish();
					}
				}
			};
			dialogRoomName.setCancelable(true);
			dialogRoomName.show(getActivity(), getActivity().getSupportFragmentManager(),
								R.string.anonymous_room_title, R.string.anonymous_room_message,
								R.integer.anonymous_name_max_chars, null);
		} else {
			dialogRoomName.show(getActivity(), getActivity().getSupportFragmentManager(),
								R.string.anonymous_room_title, R.string.anonymous_room_message,
								R.integer.anonymous_name_max_chars, null);
		}
	}

	private void startChat(Constants.ChatType chatType, Long phone, String roomName) {
		TaskConversationCreate task = new TaskConversationCreate();
		ConversationParams params = new ConversationParams(chatType, phone, roomName);
		task.execute(params, new Callback<ConversationParams, Conversation>() {
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
		});
	}

	@Override
	public void commentVotedAgree(Long commentId, Long userCommentId, int adapterPosition) {
		// nothing
	}

	@Override
	public void commentVotedDisagree(Long commentId, Long userCommentId, int adapterPosition) {
		// nothing
	}

	@Override
	public void commentNoVoted(Long commentId, Long userCommentId, int adapterPosition) {
		// nothing
	}
}
