package com.livae.ff.app.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.livae.ff.api.ff.model.Conversation;
import com.livae.ff.app.R;
import com.livae.ff.app.activity.AbstractActivity;
import com.livae.ff.app.activity.AbstractChatActivity;
import com.livae.ff.app.activity.ChatPrivateActivity;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.dialog.EditTextDialogFragment;
import com.livae.ff.app.task.ConversationParams;
import com.livae.ff.app.task.FlagConversation;
import com.livae.ff.app.task.ListResult;
import com.livae.ff.app.task.QueryId;
import com.livae.ff.app.task.TaskConversationCreate;
import com.livae.ff.app.task.TaskConversationUserBlock;
import com.livae.ff.app.task.TaskUserBlock;
import com.livae.ff.app.task.TaskUserUnblock;
import com.livae.ff.app.viewholders.CommentViewHolder;
import com.livae.ff.common.Constants;
import com.livae.ff.common.Constants.ChatType;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatPrivateFragment extends AbstractChatFragment implements ActionMode.Callback {

	private boolean endPreviousMessages;

	private EditTextDialogFragment dialogRoomName;

	private boolean firstTimeLoad;

	private ActionMode actionMode;

	private MenuItem menuBlock;

	private MenuItem menuUnblock;

	private boolean userBlocked;

	private Long userId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		firstTimeLoad = savedInstanceState == null;
		Intent intent = getActivity().getIntent();
		userBlocked = intent.getBooleanExtra(AbstractChatActivity.EXTRA_USER_BLOCKED, false);
		userId = intent.getLongExtra(AbstractChatActivity.EXTRA_PHONE_NUMBER, 0L);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		registerForContextMenu(recyclerView);
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
	protected void showSendMessagesPanel() {
		if (!userBlocked) {
			super.showSendMessagesPanel();
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
	public boolean onLongClick(CommentViewHolder holder) {
		if (actionMode == null) {
			actionMode = getActivity().startActionMode(this);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(CommentViewHolder holder) {
		if (actionMode != null) {
			toggleSelection(holder.getAdapterPosition());
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_private_chat_block, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menuBlock = menu.findItem(R.id.action_user_block);
		menuUnblock = menu.findItem(R.id.action_user_unblock);
		adjustBlockMenu();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_user_block:
				if (chatType == ChatType.PRIVATE_ANONYMOUS) {
					confirmationBlockAnonymousUser();
				} else {
					confirmationBlockUser();
				}
				return true;
			case R.id.action_user_unblock:
				unblockUser();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void confirmationBlockAnonymousUser() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(anonymousNick).setMessage(R.string.confirmation_block_anonymous_user)
			   .setSingleChoiceItems(R.array.block_anonymous_user_time, -1,
									 new DialogInterface.OnClickListener() {

										 @Override
										 public void onClick(DialogInterface dialog, int which) {
											 switch (which) {
												 case 0:
													 blockUser(100);
													 break;
												 case 1:
													 blockUser(30);
													 break;
												 case 2:
													 blockUser(7);
													 break;
												 case 3:
													 blockUser(1);
													 break;
											 }
											 dialog.dismiss();

										 }
									 }).show();
	}

	private void confirmationBlockUser() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(userName).setMessage(R.string.confirmation_block_user)
			   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   blockUser();
					   dialog.dismiss();
				   }
			   }).setNegativeButton(R.string.no, null).show();
	}

	private void unblockUser() {
		TaskUserUnblock task = new TaskUserUnblock();
		task.execute(userId, new Callback<Long, Void>() {
			@Override
			public void onComplete(CustomAsyncTask<Long, Void> task, Long userPhone, Void aVoid) {
				userBlocked = false;
				if (isResumed()) {
					showSendMessagesPanel();
					adjustBlockMenu();
					Snackbar.make(getActivity().findViewById(R.id.container),
								  R.string.confirmation_user_unblocked, Snackbar.LENGTH_SHORT)
							.show();

				}
			}

			@Override
			public void onError(CustomAsyncTask<Long, Void> task, Long userPhone, Exception e) {
				if (isResumed()) {
					AbstractActivity activity = (AbstractActivity) getActivity();
					activity.showSnackBarException(e);
				}
			}
		});
	}

	private void blockUser() {
		hideSendMessagesPanel();
		TaskUserBlock task = new TaskUserBlock();
		task.execute(userId, new Callback<Long, Void>() {
			@Override
			public void onComplete(CustomAsyncTask<Long, Void> task, Long userPhone, Void aVoid) {
				userBlocked = true;
				if (isResumed()) {
					adjustBlockMenu();
					Snackbar.make(getActivity().findViewById(R.id.container),
								  R.string.confirmation_user_blocked, Snackbar.LENGTH_SHORT).show();

				}
			}

			@Override
			public void onError(CustomAsyncTask<Long, Void> task, Long userPhone, Exception e) {
				if (isResumed()) {
					showSendMessagesPanel();
					AbstractActivity activity = (AbstractActivity) getActivity();
					activity.showSnackBarException(e);
				}
			}
		});
	}

	private void blockUser(int days) {
		hideSendMessagesPanel();
		long timeHours = TimeUnit.DAYS.toHours(days);
		FlagConversation flagConversation = new FlagConversation(conversationId, timeHours);
		TaskConversationUserBlock task = new TaskConversationUserBlock();
		task.execute(flagConversation, new Callback<FlagConversation, Void>() {
			@Override
			public void onComplete(CustomAsyncTask<FlagConversation, Void> task,
								   FlagConversation flagConversation, Void aVoid) {
				userBlocked = true;
				if (isResumed()) {
					adjustBlockMenu();
					Snackbar.make(getActivity().findViewById(R.id.container),
								  R.string.confirmation_user_blocked, Snackbar.LENGTH_SHORT).show();

				}
			}

			@Override
			public void onError(CustomAsyncTask<FlagConversation, Void> task,
								FlagConversation flagConversation, Exception e) {
				if (isResumed()) {
					showSendMessagesPanel();
					AbstractActivity activity = (AbstractActivity) getActivity();
					activity.showSnackBarException(e);
				}
			}
		});
	}

	private void adjustBlockMenu() {
		menuBlock.setVisible(!userBlocked);
		menuUnblock.setVisible(userBlocked);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = actionMode.getMenuInflater();
		inflater.inflate(R.menu.menu_copy, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_copy:
				ClipboardManager clipboard;
				FragmentActivity activity = getActivity();
				clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
				int selected = commentsAdapter.getSelectedItemCount();
				List<Integer> selectedItems = commentsAdapter.getSelectedItems();
				String title = getResources().getString(R.string.copy_conversation_label);
				if (selected == 1) {
					int pos = commentsAdapter.getCursorPosition(selectedItems.get(0));
					String text = commentsAdapter.getComment(pos);
					ClipData data = ClipData.newPlainText(title, text);
					clipboard.setPrimaryClip(data);
				} else if (selected > 1) {
					String text = "";
					String me = getResources().getString(R.string.me);
					DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
																		   DateFormat.SHORT);
					for (Integer position : selectedItems) {
						int pos = commentsAdapter.getCursorPosition(position);
						text += dateFormat.format(new Date(commentsAdapter.getDate(pos))) + "\t";
						if (commentsAdapter.isMe(pos)) {
							text += me;
						} else {
							text += me;
						}
						text += ": ";
						text += commentsAdapter.getComment(pos);
					}
					ClipData data = ClipData.newPlainText(title, text);
					clipboard.setPrimaryClip(data);
				}
				actionMode.finish();
				return true;
		}
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		actionMode = null;
		commentsAdapter.clearSelections();
	}

	private void toggleSelection(int index) {
		commentsAdapter.toggleSelection(index);
		String title = getString(R.string.selected_count, commentsAdapter.getSelectedItemCount());
		actionMode.setTitle(title);
	}
}
