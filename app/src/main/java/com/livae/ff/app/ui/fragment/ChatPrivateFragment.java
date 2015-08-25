package com.livae.ff.app.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.livae.ff.api.ff.model.Conversation;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.R;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.listener.LifeCycle;
import com.livae.ff.app.provider.ContactsProvider;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.task.ConversationParams;
import com.livae.ff.app.task.FlagConversation;
import com.livae.ff.app.task.ListResult;
import com.livae.ff.app.task.QueryParam;
import com.livae.ff.app.task.TaskConversationCreate;
import com.livae.ff.app.task.TaskConversationUserBlock;
import com.livae.ff.app.task.TaskUserBlock;
import com.livae.ff.app.task.TaskUserUnblock;
import com.livae.ff.app.ui.activity.AbstractActivity;
import com.livae.ff.app.ui.activity.AbstractChatActivity;
import com.livae.ff.app.ui.activity.ChatPrivateActivity;
import com.livae.ff.app.ui.dialog.EditTextDialogFragment;
import com.livae.ff.app.ui.viewholders.CommentViewHolder;
import com.livae.ff.common.Constants;
import com.livae.ff.common.Constants.ChatType;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatPrivateFragment extends AbstractChatFragment implements ActionMode.Callback {

	protected static final int LOADER_CONTACT = LOADER_ITEM + 1;

	private boolean endPreviousMessages;

	private EditTextDialogFragment dialogRoomName;

	private boolean firstTimeLoad;

	private ActionMode actionMode;

	private MenuItem menuBlock;

	private MenuItem menuUnblock;

	private boolean userBlocked;

	private Long userId;

	private Long rawContactId;

	private Button addContactButton;

	// TODO color and tone chooser for notifications:

	/*

            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone for
            notifications:");
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            Uri currentUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri);
            startActivityForResult( intent, 999);
	 */

	private void addContactActivity() {
		Intent intent = new Intent(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
								   Uri.parse("tel:" + conversationPhone));
		intent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true);
		startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		firstTimeLoad = savedInstanceState == null;
		Intent intent = getActivity().getIntent();
		userBlocked = intent.getBooleanExtra(AbstractChatActivity.EXTRA_USER_BLOCKED, false);
		userId = intent.getLongExtra(AbstractChatActivity.EXTRA_PHONE_NUMBER, 0L);
		if (intent.hasExtra(AbstractChatActivity.EXTRA_USER_RAW_CONTACT_ID)) {
			rawContactId = intent.getLongExtra(AbstractChatActivity.EXTRA_USER_RAW_CONTACT_ID, 0L);
		}
		if (chatType != ChatType.PRIVATE_ANONYMOUS) {
			getLoaderManager().initLoader(LOADER_CONTACT, null, this);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (chatType != ChatType.PRIVATE_ANONYMOUS) {
			getLoaderManager().restartLoader(LOADER_CONTACT, null, this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable ViewGroup container,
							 @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_private_comments, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		registerForContextMenu(recyclerView);
		addContactButton = (Button) view.findViewById(R.id.button_add_contact);
		addContactButton.setOnClickListener(this);
		//noinspection PointlessBooleanExpression
		if (rawContactId != null || chatType == ChatType.PRIVATE_ANONYMOUS || BuildConfig.TEST) {
			addContactButton.setVisibility(View.GONE);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		switch (id) {
			case LOADER_CONTACT:
				return new CursorLoader(getActivity(), ContactsProvider.getUriContact(userId),
										new String[]{Table.LocalUser.ANDROID_RAW_CONTACT_ID}, null,
										null, null);
			// break
		}
		return super.onCreateLoader(id, bundle);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> objectLoader, Cursor cursor) {
		super.onLoadFinished(objectLoader, cursor);
		switch (objectLoader.getId()) {
			case LOADER_CONTACT:
				//noinspection PointlessBooleanExpression,ConstantConditions
				if (cursor.moveToFirst() && !BuildConfig.TEST) {
					int iRawContactId =
					  cursor.getColumnIndex(Table.LocalUser.ANDROID_RAW_CONTACT_ID);
					if (!cursor.isNull(iRawContactId) && rawContactId == null) {
						addContactButton.setVisibility(View.GONE);
						rawContactId = cursor.getLong(iRawContactId);
					}
				}
				cursor.close();
				break;
			case LOADER_ITEMS:
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_private_chat_block, menu);
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
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.button_add_contact:
				addContactActivity();
				break;
			default:
				super.onClick(v);
		}
	}

	@Override
	protected void showSendMessagesPanel() {
		if (!userBlocked) {
			super.showSendMessagesPanel();
		}
	}

	@Override
	protected CustomAsyncTask<AbstractFragment, QueryParam, ListResult> getLoaderTask() {
		endPreviousMessages = false;
		return new LoadPrivateCommentsTask(this);
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
		TaskConversationCreate task = new TaskConversationCreate(this);
		ConversationParams params = new ConversationParams(chatType, phone, roomName);
		task.execute(params, new Callback<LifeCycle, ConversationParams, Conversation>() {
			@Override
			public void onComplete(@NonNull LifeCycle lifeCycle,
								   ConversationParams conversationParams,
								   Conversation conversation) {
				ChatPrivateFragment f = (ChatPrivateFragment) lifeCycle;
				f.conversationId = conversation.getId();
				f.startConversation();
			}

			@Override
			public void onError(@NonNull LifeCycle lifeCycle,
								ConversationParams conversationParams,
								@NonNull Exception e) {
				ChatPrivateFragment f = (ChatPrivateFragment) lifeCycle;
				AbstractActivity abstractActivity = (AbstractActivity) f.getActivity();
				abstractActivity.showSnackBarException(e);
			}
		});
	}

	@Override
	public boolean onLongClick(CommentViewHolder holder) {
		if (actionMode == null) {
			actionMode = getActivity().startActionMode(this);
			toggleSelection(holder);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(CommentViewHolder holder) {
		if (actionMode != null) {
			toggleSelection(holder);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menuBlock = menu.findItem(R.id.action_user_block);
		menuUnblock = menu.findItem(R.id.action_user_unblock);
		adjustBlockMenu();
	}

	private void confirmationBlockAnonymousUser() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final AlertDialog dialog;
		dialog = builder.setTitle(anonymousNick)
						.setMessage(R.string.confirmation_block_anonymous_user)
						.setNegativeButton(R.string.cancel, null)
						.setView(R.layout.view_block_buttons)
						.create();
		dialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(final DialogInterface dialogInterface) {
				View.OnClickListener clickListener = new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						switch (v.getId()) {
							case R.id.button_block_100_days:
								blockUser(100);
								break;
							case R.id.button_block_30_days:
								blockUser(30);
								break;
							case R.id.button_block_7_days:
								blockUser(7);
								break;
							case R.id.button_block_1_day:
								blockUser(1);
								break;
						}
						dialogInterface.dismiss();
					}
				};
				dialog.findViewById(R.id.button_block_100_days).setOnClickListener(clickListener);
				dialog.findViewById(R.id.button_block_30_days).setOnClickListener(clickListener);
				dialog.findViewById(R.id.button_block_7_days).setOnClickListener(clickListener);
				dialog.findViewById(R.id.button_block_1_day).setOnClickListener(clickListener);
			}
		});
		dialog.show();
	}

	private void confirmationBlockUser() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(userName)
			   .setMessage(R.string.confirmation_block_user)
			   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   blockUser();
					   dialog.dismiss();
				   }
			   })
			   .setNegativeButton(R.string.no, null)
			   .show();
	}

	private void unblockUser() {
		TaskUserUnblock task = new TaskUserUnblock(this);
		task.execute(userId, new Callback<LifeCycle, Long, Void>() {
			@Override
			public void onComplete(@NonNull LifeCycle lifeCycle, Long userPhone, Void aVoid) {
				ChatPrivateFragment f = (ChatPrivateFragment) lifeCycle;
				f.userBlocked = false;
				f.showSendMessagesPanel();
				f.adjustBlockMenu();
				Snackbar.make(f.getActivity().findViewById(R.id.container),
							  R.string.confirmation_user_unblocked, Snackbar.LENGTH_SHORT).show();

			}

			@Override
			public void onError(@NonNull LifeCycle lifeCycle, Long userPhone, Exception e) {
				ChatPrivateFragment f = (ChatPrivateFragment) lifeCycle;
				AbstractActivity activity = (AbstractActivity) f.getActivity();
				activity.showSnackBarException(e);
			}
		});
	}

	private void blockUser() {
		hideSendMessagesPanel();
		TaskUserBlock task = new TaskUserBlock(this);
		task.execute(userId, new Callback<LifeCycle, Long, Void>() {
			@Override
			public void onComplete(@NonNull LifeCycle lifeCycle, Long userPhone, Void aVoid) {
				ChatPrivateFragment f = (ChatPrivateFragment) lifeCycle;
				f.userBlocked = true;
				f.adjustBlockMenu();
				Snackbar.make(f.getActivity().findViewById(R.id.container),
							  R.string.confirmation_user_blocked, Snackbar.LENGTH_SHORT).show();

			}

			@Override
			public void onError(@NonNull LifeCycle lifeCycle,
								Long userPhone,
								@NonNull Exception e) {
				ChatPrivateFragment f = (ChatPrivateFragment) lifeCycle;
				f.userBlocked = false;
				f.showSendMessagesPanel();
				AbstractActivity activity = (AbstractActivity) f.getActivity();
				activity.showSnackBarException(e);
			}
		});
	}

	private void blockUser(int days) {
		hideSendMessagesPanel();
		long timeHours = TimeUnit.DAYS.toHours(days);
		FlagConversation flagConversation = new FlagConversation(conversationId, timeHours);
		TaskConversationUserBlock task = new TaskConversationUserBlock(this);
		task.execute(flagConversation, new Callback<LifeCycle, FlagConversation, Void>() {
			@Override
			public void onComplete(@NonNull LifeCycle lifeCycle,
								   FlagConversation flagConversation,
								   Void aVoid) {
				ChatPrivateFragment f = (ChatPrivateFragment) lifeCycle;
				f.userBlocked = true;
				final FragmentActivity activity = f.getActivity();
				Uri uriConversation = ConversationsProvider.getUriConversation(conversationId);
				activity.getContentResolver().delete(uriConversation, null, null);
				Uri uriComments = ConversationsProvider.getUriConversationComments(conversationId);
				activity.getContentResolver().delete(uriComments, null, null);
				f.adjustBlockMenu();
				Snackbar.make(activity.findViewById(R.id.container),
							  R.string.confirmation_user_blocked, Snackbar.LENGTH_SHORT).show();
				activity.finish();
			}

			@Override
			public void onError(@NonNull LifeCycle lifeCycle,
								FlagConversation flagConversation,
								@NonNull Exception e) {
				ChatPrivateFragment f = (ChatPrivateFragment) lifeCycle;
				f.userBlocked = false;
				f.showSendMessagesPanel();
				AbstractActivity activity = (AbstractActivity) f.getActivity();
				activity.showSnackBarException(e);
			}
		});
	}

	private void adjustBlockMenu() {
		menuBlock.setVisible(!userBlocked);
		menuUnblock.setVisible(userBlocked);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		actionMode = mode;
		MenuInflater inflater = mode.getMenuInflater();
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
				clipboard = (ClipboardManager) activity.getSystemService(Context
																		   .CLIPBOARD_SERVICE);
				int selected = commentsAdapter.getSelectedItemCount();
				List<Integer> selectedItems = commentsAdapter.getSelectedItems();
				String title = getResources().getString(R.string.copy_conversation_label);
				if (selected == 1) {
					int pos = selectedItems.get(0);
					String text = commentsAdapter.getComment(pos);
					ClipData data = ClipData.newPlainText(title, text);
					clipboard.setPrimaryClip(data);
				} else if (selected > 1) {
					String text = "";
					String me = getResources().getString(R.string.me);
					DateFormat dateFormat =
					  DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
					for (Integer position : selectedItems) {
						int pos = position;
						Date date = new Date(commentsAdapter.getDate(pos));
						text += "[" + dateFormat.format(date) + "] ";
						if (commentsAdapter.isMe(pos)) {
							text += me;
						} else {
							text += userName;
						}
						text += ": ";
						text += commentsAdapter.getComment(pos);
						text += "\n";
					}
					ClipData data = ClipData.newPlainText(title, text);
					clipboard.setPrimaryClip(data);
				}
				actionMode.finish();
				Snackbar.make(getActivity().findViewById(R.id.container), R.string.comments_copied,
							  Snackbar.LENGTH_SHORT).show();
				return true;
		}
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		actionMode = null;
		commentsAdapter.clearSelections();
	}

	private void toggleSelection(CommentViewHolder holder) {
		commentsAdapter.toggleSelection(holder);
		int selectedItemCount = commentsAdapter.getSelectedItemCount();
		String title = getString(R.string.selected_count, selectedItemCount);
		actionMode.setTitle(title);
		if (selectedItemCount == 0) {
			actionMode.finish();
		}
	}

	class LoadPrivateCommentsTask
	  extends CustomAsyncTask<AbstractFragment, QueryParam, ListResult> {

		LoadPrivateCommentsTask(AbstractFragment lifeCycle) {
			super(lifeCycle);
		}

		@Override
		public CustomAsyncTask<AbstractFragment, QueryParam, ListResult> //
		execute(QueryParam query, Callback<AbstractFragment, QueryParam, ListResult> callback) {
			callback.onComplete(getLifeCycle(), query, doInBackground(query));
			return this;
		}

		@Override
		protected ListResult doInBackground(QueryParam query) {
			ChatPrivateFragment f = (ChatPrivateFragment) getLifeCycle();
			int toLoad = 100;
			if (f.endPreviousMessages) {
				toLoad = 0;
			} else if (f.firstTimeLoad && f.unreadMessages != null) {
				toLoad = Math.max(f.unreadMessages + 20, toLoad);
			}
			return new ListResult(f.endPreviousMessages ? null : "", toLoad);
		}
	}
}
