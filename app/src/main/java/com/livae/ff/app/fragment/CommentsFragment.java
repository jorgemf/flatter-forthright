package com.livae.ff.app.fragment;

import android.net.Uri;
import android.support.v4.util.Pair;

import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.Analytics;
import com.livae.ff.app.activity.AbstractActivity;
import com.livae.ff.app.adapter.CommentsAdapter;
import com.livae.ff.app.adapter.EndlessCursorAdapter;
import com.livae.ff.app.async.Callback;
import com.livae.ff.app.async.CustomAsyncTask;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.CommentActionListener;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.task.ListResult;
import com.livae.ff.app.task.QueryComments;
import com.livae.ff.app.task.TaskGetUserComments;
import com.livae.ff.app.task.TaskNoVoteComment;
import com.livae.ff.app.task.TaskVoteAgreeComment;
import com.livae.ff.app.task.TaskVoteDisagreeComment;
import com.livae.ff.app.viewholders.CommentsViewHolder;
import com.livae.ff.common.Constants.ChatType;
import com.livae.ff.common.Constants.CommentVoteType;

public class CommentsFragment extends AbstractLoaderFragment<CommentsViewHolder, QueryComments>
  implements CommentActionListener {

	private TaskVoteAgreeComment taskVoteAgreeComment;

	private TaskVoteDisagreeComment taskVoteDisagreeComment;

	private TaskNoVoteComment taskNoVoteComment;

//	private TaskDeleteComment taskDeleteComment;

	private Long userId;

	private ChatType chatType;

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
	protected NetworkAsyncTask<QueryComments, ListResult> getLoaderTask() {
		return new TaskGetUserComments();
	}

	@Override
	protected Uri getUriCursor() {
		return null;
	}

	@Override
	protected EndlessCursorAdapter<CommentsViewHolder> getAdapter() {
		commentsAdapter = new CommentsAdapter(this, this);
		return commentsAdapter;
	}

	@Override
	protected String[] getProjection() {
		return CommentsAdapter.PROJECTION;
	}

	@Override
	protected QueryComments getBaseQueryParams() {
		return new QueryComments(userId, chatType);
	}

	@Override
	protected String getOrderString() {
		return "-" + Table.Comment.DATE;
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
			taskVoteDisagreeComment = new TaskVoteDisagreeComment();
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
			taskNoVoteComment = new TaskNoVoteComment();
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

}
