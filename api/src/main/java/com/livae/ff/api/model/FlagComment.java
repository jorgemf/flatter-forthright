package com.livae.ff.api.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.livae.ff.common.Constants.FlagReason;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nonnull;

@Entity
public class FlagComment implements Serializable {

	@Id
	private Long id;

	private Date date;

	@Index
	private Long commentId;

	@Index
	private Long userId;

	private FlagReason reason;

	private String comment;

	public FlagComment() {
	}

	public FlagComment(@Nonnull Long commentId, @Nonnull Long userId, @Nonnull FlagReason reason,
					   String comment) {
		this.commentId = commentId;
		this.userId = userId;
		this.reason = reason;
		this.date = new Date();
		this.comment = comment;
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

	public FlagReason getReason() {
		return reason;
	}

	public void setReason(FlagReason reason) {
		this.reason = reason;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
