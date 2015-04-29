package com.livae.ff.app.fragment;

import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.activity.AbstractActivity;
import com.livae.ff.app.activity.UserActivity;
import com.livae.ff.app.adapter.CommentsAdapter;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.listener.CommentActionListener;
import com.livae.ff.app.listener.UserClickListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.task.QueryParam;
import com.livae.ff.app.task.TaskNoVoteComment;
import com.livae.ff.app.task.TaskVoteAgreeComment;
import com.livae.ff.app.task.TaskVoteDisagreeComment;
import com.livae.ff.app.viewholders.CommentsViewHolder;
import com.livae.ff.common.Constants.CommentVoteType;

public abstract class AbstractCommentsFragment<Q extends QueryParam>
  extends AbstractLoaderFragment<CommentsViewHolder, Q>
  implements CommentActionListener, UserClickListener {

	private TaskVoteAgreeComment taskVoteAgreeComment;

	private TaskVoteDisagreeComment taskVoteDisagreeComment;

	private TaskNoVoteComment taskNoVoteComment;

//	private TaskDeleteComment taskDeleteComment;

//	private TaskUpdateComment taskUpdateComment;

	private Long userId;

	private CommentsAdapter commentsAdapter;

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
//		if (taskDeleteComment != null) {
//			taskDeleteComment.cancel();
//		}
//		if (taskUpdateComment != null) {
//			taskUpdateComment.cancel();
//		}
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
	public void commentVotedAgree(Long commentId, Long userCommentId, int adapterPosition) {
		if (taskVoteAgreeComment == null) {
			taskVoteAgreeComment = new TaskVoteAgreeComment();
		}
		Pair<Long, Integer> param = new Pair<>(commentId, adapterPosition);
		taskVoteAgreeComment.execute(param, new Callback<Pair<Long, Integer>, Comment>() {
			@Override
			public void onComplete(CustomAsyncTask<Pair<Long, Integer>, Comment> task,
								   Pair<Long, Integer> param, Comment result) {
				Analytics.event(Analytics.Category.COMMUNITY, Analytics.Action.COMMENT_VOTED_UP);
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

	@Override
	public void commentVotedDisagree(Long commentId, Long userCommentId, int adapterPosition) {
		if (taskVoteDisagreeComment == null) {
			taskVoteDisagreeComment = new TaskVoteDisagreeComment();
		}
		Pair<Long, Integer> param = new Pair<>(commentId, adapterPosition);
		taskVoteDisagreeComment.execute(param, new Callback<Pair<Long, Integer>, Comment>() {
			@Override
			public void onComplete(CustomAsyncTask<Pair<Long, Integer>, Comment> task,
								   Pair<Long, Integer> param, Comment result) {
				Analytics.event(Analytics.Category.COMMUNITY, Analytics.Action.COMMENT_VOTED_DOWN);
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

	@Override
	public void commentNoVoted(Long commentId, Long userCommentId, int adapterPosition) {
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

//	@Override
//	public void commentDelete(final Long commentId, String comment, final int adapterPosition) {
//		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		builder.setTitle(R.string.dialog_delete_comment)
//			   .setMessage(getString(R.string.dialog_delete_comment_confirmation, comment))
//			   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//				   @Override
//				   public void onClick(DialogInterface dialog, int which) {
//					   dialog.dismiss();
//				   }
//			   }).setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
//				if (taskDeleteComment == null) {
//					taskDeleteComment = new TaskDeleteComment();
//				}
//				taskDeleteComment.execute(commentId, new Callback<Long, Void>() {
//					@Override
//					public void onComplete(CustomAsyncTask<Long, Void> task, Long param,
//										   Void result) {
//						removeItem(adapterPosition);
//						if (!task.isCancelled()) {
//							AbstractActivity activity = (AbstractActivity) getActivity();
//							activity.showSnackBar(R.string.comment_deleted);
//						}
//					}
//
//					@Override
//					public void onError(CustomAsyncTask<Long, Void> task, Long param, Exception e) {
//						if (!task.isCancelled()) {
//							AbstractActivity activity = (AbstractActivity) getActivity();
//							activity.showSnackBarException(e);
//						}
//					}
//				});
//			}
//		}).show();
//	}

//	@Override
//	public void commentUpdate(final Long commentId, final String commentText,
//							  final int adapterPosition) {
//		EditTextDialogFragment dialog = new EditTextDialogFragment() {
//			@Override
//			protected void performAction(EditTextDialogFragment dialog, String newText) {
//				updateComment(dialog, commentId, newText, adapterPosition);
//			}
//		};
//		dialog.show(getActivity(), getFragmentManager(), R.string.dialog_update_comment_title,
//					R.string.dialog_update_comment_message, R.integer.comment_min_chars,
//					R.integer.comment_max_chars, commentText);
//	}

	@Override
	public void userClicked(Long userId, ImageView imageView, TextView name, TextView tagline,
							View cardView) {
		Toolbar toolbar = ((AbstractActivity) getActivity()).getToolbar();
		UserActivity.start(getActivity(), userId, imageView, name, tagline, cardView);
	}

//	private void updateComment(final EditTextDialogFragment dialog, long commentId, String text,
//							   final int adapterPosition) {
//
//		if (taskUpdateComment == null) {
//			taskUpdateComment = new TaskUpdateComment();
//		}
//		TextId textId = new TextId(text, commentId);
//		taskUpdateComment.execute(textId, new Callback<TextId, Comment>() {
//			@Override
//			public void onComplete(CustomAsyncTask<TextId, Comment> task, TextId param,
//								   Comment result) {
//				Analytics.event(Analytics.Category.COMMUNITY, Analytics.Action.COMMENT_UPDATED);
//				commentsAdapter.setCommentText(result.getId(), result.getComment());
//				commentsAdapter.notifyItemChanged(adapterPosition);
//				if (!task.isCancelled()) {
//					dialog.dismiss();
//				}
//			}
//
//			@Override
//			public void onError(CustomAsyncTask<TextId, Comment> task, TextId param, Exception e) {
//				if (!task.isCancelled()) {
//					dialog.retry();
//					AbstractActivity activity = (AbstractActivity) getActivity();
//					activity.showSnackBarException(e);
//				}
//			}
//		});
//	}
}
