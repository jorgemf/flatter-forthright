package com.livae.ff.app.listener;

public interface CommentActionListener {

	public void commentVotedUp(Long commentId, Long userCommentId, int adapterPosition);

	public void commentVotedDown(Long commentId, Long userCommentId, int adapterPosition);

	public void commentNoVoted(Long commentId, Long userCommentId, int adapterPosition);

	public void commentDelete(Long commentId, String comment, int adapterPosition);

	public void commentUpdate(Long commentId, String commentText, int adapterPosition);

}
