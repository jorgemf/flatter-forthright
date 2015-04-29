package com.livae.ff.app.listener;

public interface CommentActionListener {

	public void commentVotedAgree(Long commentId, Long userCommentId, int adapterPosition);

	public void commentVotedDisagree(Long commentId, Long userCommentId, int adapterPosition);

	public void commentNoVoted(Long commentId, Long userCommentId, int adapterPosition);

//	public void commentDelete(Long commentId, String comment, int adapterPosition);

}
