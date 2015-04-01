package com.livae.ff.api.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfTrue;
import com.livae.ff.common.Constants.CommentVoteType;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nonnull;

import static com.livae.ff.api.OfyService.ofy;

@Entity
@SuppressWarnings("UnusedDeclaration")
public class Comment implements Serializable {

	@Id
	private Long id;

	@Index
	private Long userId;

	@Index
	private Date date;

	private String comment;

	private Integer upVotes;

	private Integer downVotes;

	@Index
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Integer votes;

	@Index(IfTrue.class)
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Boolean isDownVoted;

	@Ignore
	private CommentVoteType voteType;

	public Comment() {
	}

	public Comment(@Nonnull Long userId, @Nonnull String comment) {
		this.userId = userId;
		this.comment = comment;
		this.isDownVoted = false;
		date = new Date();
		upVotes = 0;
		downVotes = 0;
		votes = 0;
	}

	public static Comment get(Long id) {
		return ofy().load().type(Comment.class).id(id).now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer getUpVotes() {
		return upVotes;
	}

	public void setUpVotes(Integer upVotes) {
		this.upVotes = upVotes;
		this.votes = this.upVotes - this.downVotes;
		this.isDownVoted = votes < 0;
	}

	public Integer getDownVotes() {
		return downVotes;
	}

	public void setDownVotes(Integer downVotes) {
		this.downVotes = downVotes;
		this.votes = this.upVotes - this.downVotes;
		this.isDownVoted = votes < 0;
	}

	public Integer getVotes() {
		return votes;
	}

	public void setVotes(Integer votes) {
		this.votes = votes;
	}

	public CommentVoteType getVoteType() {
		return voteType;
	}

	public void setVoteType(CommentVoteType voteType) {
		this.voteType = voteType;
	}
}
