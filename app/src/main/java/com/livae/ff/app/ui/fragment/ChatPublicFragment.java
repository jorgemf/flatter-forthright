package com.livae.ff.app.ui.fragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
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
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.listener.LifeCycle;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.task.ConversationParams;
import com.livae.ff.app.task.FlagComment;
import com.livae.ff.app.task.ListResult;
import com.livae.ff.app.task.QueryParam;
import com.livae.ff.app.task.TaskCommentFlag;
import com.livae.ff.app.task.TaskCommentVoteAgree;
import com.livae.ff.app.task.TaskCommentVoteDelete;
import com.livae.ff.app.task.TaskCommentVoteDisagree;
import com.livae.ff.app.task.TaskCommentsGet;
import com.livae.ff.app.task.TaskConversationCreate;
import com.livae.ff.app.task.TaskForthrightBlock;
import com.livae.ff.app.task.TaskForthrightUnblock;
import com.livae.ff.app.ui.activity.AbstractActivity;
import com.livae.ff.app.ui.activity.AbstractChatActivity;
import com.livae.ff.app.ui.dialog.EditTextDialogFragment;
import com.livae.ff.app.ui.view.ContextMenuRecyclerView;
import com.livae.ff.app.ui.viewholders.CommentViewHolder;
import com.livae.ff.common.Constants;
import com.livae.ff.common.Constants.CommentVoteType;

public class ChatPublicFragment extends AbstractChatFragment {

	private boolean isMyPublicChat;

	private MenuItem editMenuItem;

	private EditTextDialogFragment dialog;

	private MenuItem menuBlock;

	private MenuItem menuUnblock;

	private Pair<Long, Integer> savedContextMenuInfo;

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
	protected void getConversation() {
		ConversationParams conversationParams = new ConversationParams(chatType,
																	   conversationPhone);
		Callback<LifeCycle, ConversationParams, Conversation> callback;
		callback = new Callback<LifeCycle, ConversationParams, Conversation>() {
			@Override
			public void onComplete(@NonNull LifeCycle lifeCycle,
								   ConversationParams conversationParams,
								   Conversation conversation) {
				ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				f.conversationId = conversation.getId();
				f.startConversation();
			}

			@Override
			public void onError(@NonNull LifeCycle lifeCycle,
								ConversationParams conversationParams,
								@NonNull Exception e) {
				ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				AbstractActivity abstractActivity = (AbstractActivity) f.getActivity();
				abstractActivity.showSnackBarException(e);
			}
		};
		new TaskConversationCreate(this).execute(conversationParams, callback);
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
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (!isMyPublicChat) {
			editMenuItem = menu.findItem(R.id.action_edit);
		}
		menuBlock = menu.findItem(R.id.action_public_block);
		menuUnblock = menu.findItem(R.id.action_public_unblock);
		adjustBlockMenu();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
									ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ContextMenuRecyclerView.RecyclerContextMenuInfo recyclerInfo =
		  (ContextMenuRecyclerView.RecyclerContextMenuInfo) menuInfo;
		CommentViewHolder viewHolder = (CommentViewHolder) recyclerInfo.viewHolder;
		String comment = viewHolder.getComment();
		if (!TextUtils.isEmpty(comment)) { // if it is empty, chat was probably blocked
			if (comment.length() > 50) {
				comment = comment.substring(0, 49) + "…";
			}
			menu.setHeaderTitle(comment);
			MenuInflater inflater = getActivity().getMenuInflater();
			inflater.inflate(R.menu.menu_comment, menu);
			int cursorPosition = viewHolder.getAdapterPosition();
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
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
		ContextMenuRecyclerView.RecyclerContextMenuInfo recyclerInfo;
		recyclerInfo = (ContextMenuRecyclerView.RecyclerContextMenuInfo) menuInfo;
		Long commentId;
		int adapterPosition;
		if (recyclerInfo == null) {
			commentId = savedContextMenuInfo.first;
			adapterPosition = savedContextMenuInfo.second;
		} else {
			CommentViewHolder viewHolder;
			viewHolder = (CommentViewHolder) recyclerInfo.viewHolder;
			commentId = viewHolder.getCommentId();
			adapterPosition = viewHolder.getAdapterPosition();
		}
		if (commentId != null) {
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
				case R.id.action_flag:
					savedContextMenuInfo = new Pair<>(commentId, adapterPosition);
					// no break
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
			if (blockedForthRightChats == null) {
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
		TaskForthrightUnblock task = new TaskForthrightUnblock(this);
		task.execute(null, new Callback<LifeCycle, Void, Void>() {
			@Override
			public void onComplete(@NonNull LifeCycle lifeCycle, Void params, Void result) {
				ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				f.adjustBlockMenu();
				Snackbar.make(f.getActivity().findViewById(R.id.container),
							  R.string.confirmation_forthright_unblocked, Snackbar.LENGTH_SHORT)
						.show();
				f.restart();
			}

			@Override
			public void onError(@NonNull LifeCycle lifeCycle, Void params, @NonNull Exception e) {
				ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				AbstractActivity activity = (AbstractActivity) f.getActivity();
				activity.showSnackBarException(e);
			}
		});
	}

	private void blockChat() {
		TaskForthrightBlock task = new TaskForthrightBlock(this);
		task.execute(null, new Callback<LifeCycle, Void, Void>() {
			@Override
			public void onComplete(@NonNull LifeCycle lifeCycle, Void params, Void result) {
				ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				f.adjustBlockMenu();
				Snackbar.make(f.getActivity().findViewById(R.id.container),
							  R.string.confirmation_forthright_blocked, Snackbar.LENGTH_SHORT)
						.show();
			}

			@Override
			public void onError(@NonNull LifeCycle lifeCycle, Void params, @NonNull Exception e) {
				ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				AbstractActivity activity = (AbstractActivity) f.getActivity();
				activity.showSnackBarException(e);
			}
		});
	}

	@Override
	protected CustomAsyncTask<AbstractFragment, QueryParam, ListResult> getLoaderTask() {
		return new TaskCommentsGet(this);
	}

	private void commentFlag(Long commentId, int adapterPosition, Constants.FlagReason flagReason,
							 String comment) {
		TaskCommentFlag taskCommentFlag = new TaskCommentFlag(this);
		FlagComment flagComment = new FlagComment();
		flagComment.setCommentId(commentId);
		flagComment.setComment(comment);
		flagComment.setReason(flagReason);
		Pair<FlagComment, Integer> param = new Pair<>(flagComment, adapterPosition);
		final Callback<LifeCycle, Pair<FlagComment, Integer>, Void> callback =
		  new Callback<LifeCycle, Pair<FlagComment, Integer>, Void>() {
			  @Override
			  public void onComplete(@NonNull LifeCycle lifeCycle,
									 Pair<FlagComment, Integer> param,
									 Void aVoid) {
				  ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				  Analytics.event(Analytics.Category.CONTENT, Analytics.Action.COMMENT_FLAGGED,
								  param.first.getReason().name());
				  if (isResumed()) {
					  commentsAdapter.notifyItemChanged(param.second);
					  AbstractActivity activity = (AbstractActivity) getActivity();
					  Snackbar.make(activity.findViewById(R.id.container), R.string
																			 .comment_flagged,
									Snackbar.LENGTH_SHORT).show();
				  }
			  }

			  @Override
			  public void onError(@NonNull LifeCycle lifeCycle,
								  Pair<FlagComment, Integer> param,
								  @NonNull Exception e) {
				  ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				  if (isResumed()) {
					  commentsAdapter.notifyItemChanged(param.second);
					  AbstractActivity activity = (AbstractActivity) getActivity();
					  activity.showSnackBarException(e);
				  }
			  }
		  };
		taskCommentFlag.execute(param, callback);
	}

	private void commentVotedAgree(Long commentId, int adapterPosition) {
		TaskCommentVoteAgree taskVoteAgreeComment = new TaskCommentVoteAgree(this);
		Pair<Long, Integer> param = new Pair<>(commentId, adapterPosition);
		commentsAdapter.votedComment(commentId, CommentVoteType.AGREE);
		commentsAdapter.notifyItemChanged(adapterPosition);
		final Callback<LifeCycle, Pair<Long, Integer>, Comment> callback =
		  new Callback<LifeCycle, Pair<Long, Integer>, Comment>() {

			  @Override
			  public void onComplete(@NonNull LifeCycle lifeCycle,
									 Pair<Long, Integer> param,
									 Comment comment) {
				  ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				  Analytics.event(Analytics.Category.CONTENT, Analytics.Action
																.COMMENT_VOTED_AGREE);
				  f.commentsAdapter.notifyItemChanged(param.second);
			  }

			  @Override
			  public void onError(@NonNull LifeCycle lifeCycle,
								  Pair<Long, Integer> param,
								  @NonNull Exception e) {
				  ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				  f.commentsAdapter.removeVote(param.first);
				  f.commentsAdapter.notifyItemChanged(param.second);
				  AbstractActivity activity = (AbstractActivity) f.getActivity();
				  activity.showSnackBarException(e);
			  }
		  };
		taskVoteAgreeComment.execute(param, callback);
	}

	private void commentVotedDisagree(Long commentId, int adapterPosition) {
		TaskCommentVoteDisagree taskVoteDisagreeComment = new TaskCommentVoteDisagree(this);
		Pair<Long, Integer> param = new Pair<>(commentId, adapterPosition);
		commentsAdapter.votedComment(commentId, CommentVoteType.DISAGREE);
		commentsAdapter.notifyItemChanged(adapterPosition);
		final Callback<LifeCycle, Pair<Long, Integer>, Comment> callback =
		  new Callback<LifeCycle, Pair<Long, Integer>, Comment>() {
			  @Override
			  public void onComplete(@NonNull LifeCycle lifeCycle,
									 Pair<Long, Integer> param,
									 Comment comment) {
				  ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				  Analytics.event(Analytics.Category.CONTENT,
								  Analytics.Action.COMMENT_VOTED_DISAGREE);
				  f.commentsAdapter.notifyItemChanged(param.second);
			  }

			  @Override
			  public void onError(@NonNull LifeCycle lifeCycle,
								  Pair<Long, Integer> param,
								  @NonNull Exception e) {
				  ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				  f.commentsAdapter.removeVote(param.first);
				  f.commentsAdapter.notifyItemChanged(param.second);
				  AbstractActivity activity = (AbstractActivity) f.getActivity();
				  activity.showSnackBarException(e);
			  }
		  };
		taskVoteDisagreeComment.execute(param, callback);
	}

	public void commentNoVoted(Long commentId, int adapterPosition) {
		TaskCommentVoteDelete taskNoVoteComment = new TaskCommentVoteDelete(this);
		commentsAdapter.votedComment(commentId, null);
		commentsAdapter.notifyItemChanged(adapterPosition);
		Pair<Long, Integer> param = new Pair<>(commentId, adapterPosition);
		final Callback<LifeCycle, Pair<Long, Integer>, Comment> callback =
		  new Callback<LifeCycle, Pair<Long, Integer>, Comment>() {
			  @Override
			  public void onComplete(@NonNull LifeCycle lifeCycle,
									 Pair<Long, Integer> param,
									 Comment comment) {
				  ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				  Analytics.event(Analytics.Category.CONTENT,
								  Analytics.Action.COMMENT_VOTE_REMOVED);
				  f.commentsAdapter.notifyItemChanged(param.second);
			  }

			  @Override
			  public void onError(@NonNull LifeCycle lifeCycle,
								  Pair<Long, Integer> param,
								  @NonNull Exception e) {
				  ChatPublicFragment f = (ChatPublicFragment) lifeCycle;
				  f.commentsAdapter.removeVote(param.first);
				  f.commentsAdapter.notifyItemChanged(param.second);
				  AbstractActivity activity = (AbstractActivity) f.getActivity();
				  activity.showSnackBarException(e);
			  }
		  };
		taskNoVoteComment.execute(param, callback);
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
					contentResolver.update(ConversationsProvider.getUriConversation
																   (conversationId),
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
