package com.livae.ff.app.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.livae.android.loading.CursorRecyclerAdapter;
import com.livae.apphunt.api.apphunt.model.Comment;
import com.livae.apphunt.app.Analytics;
import com.livae.apphunt.app.AppUser;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.R;
import com.livae.apphunt.app.activity.AbstractActivity;
import com.livae.apphunt.app.activity.ApplicationActivity;
import com.livae.apphunt.app.activity.UserActivity;
import com.livae.apphunt.app.adapter.CommentsAdapter;
import com.livae.apphunt.app.async.Callback;
import com.livae.apphunt.app.async.CustomAsyncTask;
import com.livae.apphunt.app.dialog.EditTextDialogFragment;
import com.livae.apphunt.app.dialog.MakerInforDialogFragment;
import com.livae.apphunt.app.listener.ApplicationClickListener;
import com.livae.apphunt.app.listener.CommentActionListener;
import com.livae.apphunt.app.listener.LoginListener;
import com.livae.apphunt.app.listener.UserClickListener;
import com.livae.apphunt.app.sql.Table;
import com.livae.apphunt.app.task.QueryParam;
import com.livae.apphunt.app.task.TaskDeleteComment;
import com.livae.apphunt.app.task.TaskNoVoteComment;
import com.livae.apphunt.app.task.TaskUpdateComment;
import com.livae.apphunt.app.task.TaskVoteDownComment;
import com.livae.apphunt.app.task.TaskVoteUpComment;
import com.livae.apphunt.app.task.TextId;
import com.livae.apphunt.app.utils.LoginUtils;
import com.livae.apphunt.app.viewholders.CommentsViewHolder;
import com.livae.apphunt.common.Constants.CommentVoteType;
import com.livae.apphunt.common.Constants.Order;

public abstract class AbstractCommentsFragment<Q extends QueryParam>
  extends AbstractLoaderFragment<CommentsViewHolder, Q>
  implements CommentActionListener, UserClickListener, ApplicationClickListener, LoginListener {

	protected boolean isUserAnonymous;

	private TaskVoteUpComment taskVoteUpComment;

	private TaskVoteDownComment taskVoteDownComment;

	private TaskNoVoteComment taskNoVoteComment;

	private TaskDeleteComment taskDeleteComment;

	private TaskUpdateComment taskUpdateComment;

	private Long userId;

	private CommentsAdapter commentsAdapter;

	@Override
	public void onResume() {
		super.onResume();
		AppUser appUser = Application.appUser();
		isUserAnonymous = appUser.isAnonymous();
		userId = appUser.getUserId();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (taskVoteUpComment != null) {
			taskVoteUpComment.cancel();
		}
		if (taskVoteDownComment != null) {
			taskVoteDownComment.cancel();
		}
		if (taskNoVoteComment != null) {
			taskNoVoteComment.cancel();
		}
		if (taskDeleteComment != null) {
			taskDeleteComment.cancel();
		}
		if (taskUpdateComment != null) {
			taskUpdateComment.cancel();
		}
	}

	@Override
	protected CursorRecyclerAdapter<CommentsViewHolder> getAdapter() {
		commentsAdapter = new CommentsAdapter(getActivity(), this, this, this);
		return commentsAdapter;
	}

	@Override
	protected String[] getProjection() {
		return CommentsAdapter.PROJECTION;
	}

	@Override
	protected String getOrderString(Order order) {
		String orderString = null;
		switch (order) {
			case VOTES:
				orderString = "(" + Table.Comment.DOWN_VOTES + "-" + Table.Comment.UP_VOTES + ")" +
							  " , -" + Table.Comment.DATE;
				break;
			case DATE:
				orderString = "-" + Table.Comment.DATE;
				break;
		}
		return orderString;
	}

	@Override
	public void commentVotedUp(Long commentId, Long userCommentId, int adapterPosition) {
		if (isUserAnonymous) {
			LoginUtils.showRequestLogin(getActivity(), this);
		} else if (userCommentId.equals(userId)) {
			AbstractActivity activity = (AbstractActivity) getActivity();
			activity.showSnackBar(R.string.error_user_cannot_vote_its_comments);
		} else {
			if (taskVoteUpComment == null) {
				taskVoteUpComment = new TaskVoteUpComment();
			}
			Pair<Long, Integer> param = new Pair<>(commentId, adapterPosition);
			taskVoteUpComment.execute(param, new Callback<Pair<Long, Integer>, Comment>() {
				@Override
				public void onComplete(CustomAsyncTask<Pair<Long, Integer>, Comment> task,
									   Pair<Long, Integer> param, Comment result) {
					Analytics.event(Analytics.Category.COMMUNITY,
									Analytics.Action.COMMENT_VOTED_UP);
					commentsAdapter.votedComment(param.first, CommentVoteType.UP);
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
	}

	@Override
	public void commentVotedDown(Long commentId, Long userCommentId, int adapterPosition) {
		if (isUserAnonymous) {
			LoginUtils.showRequestLogin(getActivity(), this);
		} else if (userCommentId.equals(userId)) {
			AbstractActivity activity = (AbstractActivity) getActivity();
			activity.showSnackBar(R.string.error_user_cannot_vote_its_comments);
		} else {
			if (taskVoteDownComment == null) {
				taskVoteDownComment = new TaskVoteDownComment();
			}
			Pair<Long, Integer> param = new Pair<>(commentId, adapterPosition);
			taskVoteDownComment.execute(param, new Callback<Pair<Long, Integer>, Comment>() {
				@Override
				public void onComplete(CustomAsyncTask<Pair<Long, Integer>, Comment> task,
									   Pair<Long, Integer> param, Comment result) {
					Analytics.event(Analytics.Category.COMMUNITY,
									Analytics.Action.COMMENT_VOTED_DOWN);
					commentsAdapter.votedComment(param.first, CommentVoteType.DOWN);
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
	}

	@Override
	public void commentNoVoted(Long commentId, Long userCommentId, int adapterPosition) {
		if (isUserAnonymous) {
			LoginUtils.showRequestLogin(getActivity(), this);
		} else if (userCommentId.equals(userId)) {
			AbstractActivity activity = (AbstractActivity) getActivity();
			activity.showSnackBar(R.string.error_user_cannot_vote_its_comments);
		} else {
			if (taskNoVoteComment == null) {
				taskNoVoteComment = new TaskNoVoteComment();
			}
			Pair<Long, Integer> param = new Pair<>(commentId, adapterPosition);
			taskNoVoteComment.execute(param, new Callback<Pair<Long, Integer>, Comment>() {
				@Override
				public void onComplete(CustomAsyncTask<Pair<Long, Integer>, Comment> task,
									   Pair<Long, Integer> param, Comment result) {
					Analytics.event(Analytics.Category.COMMUNITY,
									Analytics.Action.COMMENT_VOTE_REMOVED);
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
	}

	@Override
	public void commentDelete(final Long commentId, String comment, final int adapterPosition) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_delete_comment)
			   .setMessage(getString(R.string.dialog_delete_comment_confirmation, comment))
			   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   dialog.dismiss();
				   }
			   }).setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (taskDeleteComment == null) {
					taskDeleteComment = new TaskDeleteComment();
				}
				taskDeleteComment.execute(commentId, new Callback<Long, Void>() {
					@Override
					public void onComplete(CustomAsyncTask<Long, Void> task, Long param,
										   Void result) {
						removeItem(adapterPosition);
						if (!task.isCancelled()) {
							AbstractActivity activity = (AbstractActivity) getActivity();
							activity.showSnackBar(R.string.comment_deleted);
						}
					}

					@Override
					public void onError(CustomAsyncTask<Long, Void> task, Long param, Exception e) {
						if (!task.isCancelled()) {
							AbstractActivity activity = (AbstractActivity) getActivity();
							activity.showSnackBarException(e);
						}
					}
				});
			}
		}).show();
	}

	@Override
	public void commentUpdate(final Long commentId, final String commentText,
							  final int adapterPosition) {
		EditTextDialogFragment dialog = new EditTextDialogFragment() {
			@Override
			protected void performAction(EditTextDialogFragment dialog, String newText) {
				updateComment(dialog, commentId, newText, adapterPosition);
			}
		};
		dialog.show(getActivity(), getFragmentManager(), R.string.dialog_update_comment_title,
					R.string.dialog_update_comment_message, R.integer.comment_min_chars,
					R.integer.comment_max_chars, commentText);
	}

	@Override
	public void userClicked(Long userId, ImageView imageView, TextView name, TextView tagline,
							View cardView) {
		Toolbar toolbar = ((AbstractActivity) getActivity()).getToolbar();
		UserActivity.start(getActivity(), userId, imageView, name, tagline, cardView);
	}

	@Override
	public void userRelationshipClicked() {
		MakerInforDialogFragment dialog = new MakerInforDialogFragment();
		dialog.show(getFragmentManager(), null);
	}

	@Override
	public void applicationClicked(Long id, String appId, ImageView image, TextView title,
								   TextView description, ToggleButton voteButton) {
		Toolbar toolbar = ((AbstractActivity) getActivity()).getToolbar();
		ApplicationActivity.start(getActivity(), id, image, title, description, voteButton);
	}

	@Override
	public void login() {
		AppUser appUser = Application.appUser();
		isUserAnonymous = appUser.isAnonymous();
		userId = appUser.getUserId();
	}

	@Override
	public void logout() {
		// nothing
	}

	private void updateComment(final EditTextDialogFragment dialog, long commentId, String text,
							   final int adapterPosition) {

		if (taskUpdateComment == null) {
			taskUpdateComment = new TaskUpdateComment();
		}
		TextId textId = new TextId(text, commentId);
		taskUpdateComment.execute(textId, new Callback<TextId, Comment>() {
			@Override
			public void onComplete(CustomAsyncTask<TextId, Comment> task, TextId param,
								   Comment result) {
				Analytics.event(Analytics.Category.COMMUNITY, Analytics.Action.COMMENT_UPDATED);
				commentsAdapter.setCommentText(result.getId(), result.getComment());
				commentsAdapter.notifyItemChanged(adapterPosition);
				if (!task.isCancelled()) {
					dialog.dismiss();
				}
			}

			@Override
			public void onError(CustomAsyncTask<TextId, Comment> task, TextId param, Exception e) {
				if (!task.isCancelled()) {
					dialog.retry();
					AbstractActivity activity = (AbstractActivity) getActivity();
					activity.showSnackBarException(e);
				}
			}
		});
	}
}
