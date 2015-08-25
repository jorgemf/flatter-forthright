package com.livae.ff.api.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.livae.ff.common.Constants.CommentVoteType;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nonnull;

@Entity
@Cache
public class CommentVote implements Serializable {

	@Id
	private Long id;

	private Date date;

	@Index
	private Long commentId;

	@Index
	private Long userId;

	private CommentVoteType type;

	public CommentVote() {
	}

	public CommentVote(@Nonnull Long commentId,
					   @Nonnull Long userId,
					   @Nonnull CommentVoteType type) {
		this.commentId = commentId;
		this.userId = userId;
		this.type = type;
		this.date = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Long getCommentId() {
		return commentId;
	}

	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public CommentVoteType getType() {
		return type;
	}

	public void setType(CommentVoteType type) {
		this.type = type;
	}
}
