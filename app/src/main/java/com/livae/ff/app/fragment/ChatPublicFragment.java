package com.livae.ff.app.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
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
import com.livae.ff.app.dialog.EditTextDialogFragment;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.task.ConversationParams;
import com.livae.ff.app.task.FlagComment;
import com.livae.ff.app.task.ListResult;
import com.livae.ff.app.task.QueryId;
import com.livae.ff.app.task.TaskCommentFlag;
import com.livae.ff.app.task.TaskCommentVoteAgree;
import com.livae.ff.app.task.TaskCommentVoteDelete;
import com.livae.ff.app.task.TaskCommentVoteDisagree;
import com.livae.ff.app.task.TaskCommentsGet;
import com.livae.ff.app.task.TaskConversationCreate;
import com.livae.ff.app.task.TaskForthrightBlock;
import com.livae.ff.app.task.TaskForthrightUnblock;
import com.livae.ff.app.view.ContextMenuRecyclerView;
import com.livae.ff.app.viewholders.CommentViewHolder;
import com.livae.ff.common.Constants;
import com.livae.ff.common.Constants.CommentVoteType;

public class ChatPublicFragment extends AbstractChatFragment {

	private TaskCommentVoteAgree taskVoteAgreeComment;

	private TaskCommentVoteDisagree taskVoteDisagreeComment;

	private TaskCommentVoteDelete taskNoVoteComment;

	private boolean isMyPublicChat;

	private MenuItem editMenuItem;

	private EditTextDialogFragment dialog;

	private MenuItem menuBlock;

	private MenuItem menuUnblock;

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
		registerForContextMenu(recyclerView);
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
		if (!isMyPublicChat) {
			inflater.inflate(R.menu.menu_edit, menu);
		}
		if (isMyPublicChat && chatType == Constants.ChatType.FORTHRIGHT) {
			inflater.inflate(R.menu.menu_public_chat_block, menu);
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		editMenuItem = menu.findItem(R.id.action_edit);
		if (anonymousNick == null) {
			editMenuItem.setVisible(false);
		}
		menuBlock = menu.findItem(R.id.action_public_block);
		menuUnblock = menu.findItem(R.id.action_public_unblock);
		adjustBlockMenu();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_edit:
				requestNickName();
				return true;
			case R.id.action_public_block:
				confirmationBlockForthrightChat();
				return true;
			case R.id.action_public_unblock:
				confirmationUnblockForthrightChat();
				return true;
		}
		return super.onOptionsItemSelected(item);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
									ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ContextMenuRecyclerView.RecyclerContextMenuInfo recyclerInfo = (ContextMenuRecyclerView.RecyclerContextMenuInfo) menuInfo;
		CommentViewHolder viewHolder = (CommentViewHolder) recyclerInfo.viewHolder;
		String comment = viewHolder.getComment();
		if (comment.length() > 50) {
			comment = comment.substring(0, 49) + "…";
		}
		menu.setHeaderTitle(comment);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.menu_comment, menu);
		int cursorPosition = commentsAdapter.getCursorPosition(viewHolder.getAdapterPosition());
		MenuItem menuItem = menu.findItem(R.id.action_flag);
		menuItem.setVisible(!commentsAdapter.isMe(cursorPosition));
		CommentVoteType commentVoteType = commentsAdapter.getVote(cursorPosition);
		if (commentVoteType == null) {
			menu.findItem(R.id.action_indifferent).setEnabled(false);
		} else {
			switch (commentVoteType) {
				case AGREE:
					menu.findItem(R.id.action_agree).setEnabled(false);
					break;
				case DISAGREE:
					menu.findItem(R.id.action_disagree).setEnabled(false);
					break;
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
		ContextMenuRecyclerView.RecyclerContextMenuInfo recyclerInfo = (ContextMenuRecyclerView.RecyclerContextMenuInfo) menuInfo;
		CommentViewHolder viewHolder = (CommentViewHolder) recyclerInfo.viewHolder;
		if (viewHolder != null) {
			Long commentId = viewHolder.getCommentId();
			int adapterPosition = viewHolder.getAdapterPosition();
			switch (item.getItemId()) {
				case R.id.action_agree:
					commentVotedAgree(commentId, adapterPosition);
					break;
				case R.id.action_disagree:
					commentVotedDisagree(commentId, adapterPosition);
					break;
				case R.id.action_indifferent:
					commentNoVoted(commentId, adapterPosition);
					break;
				case R.id.action_flag_abuse:
					commentFlag(commentId, adapterPosition, Constants.FlagReason.ABUSE, null);
					break;
				case R.id.action_flag_insult:
					commentFlag(commentId, adapterPosition, Constants.FlagReason.INSULT, null);
					break;
				case R.id.action_flag_lie:
					commentFlag(commentId, adapterPosition, Constants.FlagReason.LIE, null);
					break;
				case R.id.action_flag_other:
					commentFlag(commentId, adapterPosition, Constants.FlagReason.OTHER, null);
					break;
				default:
					return super.onContextItemSelected(item);
			}
			return true;
		}
		return false;
	}

	private void adjustBlockMenu() {
		if (isMyPublicChat && chatType == Constants.ChatType.FORTHRIGHT) {
			Long blockedForthRightChats = Application.appUser().getBlockedForthRightChats();
			if (blockedForthRightChats != null) {
				menuBlock.setVisible(true);
				menuUnblock.setVisible(false);
			} else {
				menuBlock.setVisible(false);
				menuUnblock.setVisible(true);
			}
		}
	}

	private void confirmationBlockForthrightChat() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.confirmation_block_forthright_chat)
			   .setPositiveButton(R.string.button_block, new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   blockChat();
					   dialog.dismiss();
				   }
			   }).setNegativeButton(R.string.cancel, null).show();
	}

	private void confirmationUnblockForthrightChat() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.confirmation_unblock_forthright_chat)
			   .setPositiveButton(R.string.button_unblock, new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   unblockChat();
					   dialog.dismiss();
				   }
			   }).setNegativeButton(R.string.cancel, null).show();
	}

	private void unblockChat() {
		TaskForthrightUnblock task = new TaskForthrightUnblock();
		task.execute(null, new Callback<Void, Void>() {
			@Override
			public void onComplete(CustomAsyncTask<Void, Void> task, Void params, Void result) {
				if (isResumed()) {
					adjustBlockMenu();
					Snackbar.make(getActivity().findViewById(R.id.container),
								  R.string.confirmation_forthright_unblocked, Snackbar.LENGTH_SHORT)
							.show();
				}
			}

			@Override
			public void onError(CustomAsyncTask<Void, Void> task, Void params, Exception e) {
				if (isResumed()) {
					AbstractActivity activity = (AbstractActivity) getActivity();
					activity.showSnackBarException(e);
				}
			}
		});
	}

	private void blockChat() {
		TaskForthrightBlock task = new TaskForthrightBlock();
		task.execute(null, new Callback<Void, Void>() {
			@Override
			public void onComplete(CustomAsyncTask<Void, Void> task, Void params, Void result) {
				if (isResumed()) {
					adjustBlockMenu();
					Snackbar.make(getActivity().findViewById(R.id.container),
								  R.string.confirmation_forthright_blocked, Snackbar.LENGTH_SHORT)
							.show();
				}
			}

			@Override
			public void onError(CustomAsyncTask<Void, Void> task, Void params, Exception e) {
				if (isResumed()) {
					AbstractActivity activity = (AbstractActivity) getActivity();
					activity.showSnackBarException(e);
				}
			}
		});
	}

	@Override
	protected CustomAsyncTask<QueryId, ListResult> getLoaderTask() {
		return new TaskCommentsGet();
	}

	private void commentFlag(Long commentId, int adapterPosition, Constants.FlagReason flagReason,
							 String comment) {
		TaskCommentFlag taskCommentFlag = new TaskCommentFlag();
		FlagComment flagComment = new FlagComment();
		flagComment.setCommentId(commentId);
		flagComment.setComment(comment);
		flagComment.setReason(flagReason);
		Pair<FlagComment, Integer> param = new Pair<>(flagComment, adapterPosition);
		taskCommentFlag.execute(param, new Callback<Pair<FlagComment, Integer>, Void>() {
			@Override
			public void onComplete(CustomAsyncTask<Pair<FlagComment, Integer>, Void> task,
								   Pair<FlagComment, Integer> param, Void aVoid) {
				Analytics.event(Analytics.Category.CONTENT, Analytics.Action.COMMENT_FLAGGED,
								param.first.getReason().name());
				if (isResumed()) {
					commentsAdapter.notifyItemChanged(param.second);
				}
			}

			@Override
			public void onError(CustomAsyncTask<Pair<FlagComment, Integer>, Void> task,
								Pair<FlagComment, Integer> param, Exception e) {
				if (isResumed()) {
					AbstractActivity activity = (AbstractActivity) getActivity();
					activity.showSnackBarException(e);
				}
			}
		});
	}

	private void commentVotedAgree(Long commentId, int adapterPosition) {
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

	private void commentVotedDisagree(Long commentId, int adapterPosition) {
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

	public void commentNoVoted(Long commentId, int adapterPosition) {
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

	@Override
	public boolean onLongClick(CommentViewHolder holder) {
		return holder.itemView.showContextMenu();
	}

	@Override
	public void onClick(CommentViewHolder holder) {
		// nothing
	}
}
