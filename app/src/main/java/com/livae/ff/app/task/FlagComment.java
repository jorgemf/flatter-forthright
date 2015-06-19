package com.livae.ff.app.task;

import com.livae.ff.common.Constants;

public class FlagComment {

	private Long commentId;

	private Constants.FlagReason reason;

	private String comment;

	public FlagComment() {
	}

	public FlagComment(Long user, Constants.FlagReason reason, String comment) {
		this.commentId = user;
		this.reason = reason;
		this.comment = comment;
	}

	public Long getCommentId() {
		return commentId;
	}

	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}

	public Constants.FlagReason getReason() {
		return reason;
	}

	public void setReason(Constants.FlagReason reason) {
		this.reason = reason;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
