package com.livae.ff.app.task;

import com.livae.ff.common.Constants.CommentType;

public class QueryComments extends QueryParam {

	private Long id;

	private CommentType commentType;

	public QueryComments(Long id, CommentType commentType, Integer limit) {
		super(limit);
		this.id = id;
		this.commentType = commentType;
	}

	public QueryComments(Long id, CommentType commentType) {
		this.id = id;
		this.commentType = commentType;
	}

	public Long getId() {
		return id;
	}

	public CommentType getCommentType() {
		return commentType;
	}

	@Override
	public String toString() {
		return "[id: " + id + "] " + "[commentType: " + commentType + "] " + super.toString();
	}
}
