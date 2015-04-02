package com.livae.ff.api.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfTrue;
import com.livae.ff.common.Constants.CommentType;
import com.livae.ff.common.Constants.CommentVoteType;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nonnull;

import static com.livae.ff.api.OfyService.ofy;

@Entity
public class Comment implements Serializable {

	@Id
	private Long id;

	private Long user;

	@Index
	private Long phone;

	@Index
	private Date date;

	@Index
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private CommentType commentType;

	private String comment;

	private Integer agreeVotes;

	private Integer disagreeVotes;

	@Index
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Integer votes;

	@Index(IfTrue.class)
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Boolean isDownVoted;

	@Ignore
	private CommentVoteType voteType;

	@Index(IfTrue.class)
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Boolean deleted;

	public Comment() {
	}

	public Comment(@Nonnull CommentType commentType, @Nonnull Long phone, @Nonnull Long user,
				   @Nonnull String comment) {
		this.commentType = commentType;
		this.user = user;
		this.phone = phone;
		this.comment = comment;
		this.isDownVoted = false;
		date = new Date();
		agreeVotes = 0;
		disagreeVotes = 0;
		votes = 0;
		deleted = false;
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

	public Long getUser() {
		return user;
	}

	public void setUser(Long user) {
		this.user = user;
	}

	public Long getPhone() {
		return phone;
	}

	public void setPhone(Long phone) {
		this.phone = phone;
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

	public Integer getAgreeVotes() {
		return agreeVotes;
	}

	public void setAgreeVotes(Integer agreeVotes) {
		this.agreeVotes = agreeVotes;
		this.votes = this.agreeVotes - this.disagreeVotes;
		this.isDownVoted = votes < 0;
	}

	public Integer getDisagreeVotes() {
		return disagreeVotes;
	}

	public void setDisagreeVotes(Integer disagreeVotes) {
		this.disagreeVotes = disagreeVotes;
		this.votes = this.agreeVotes - this.disagreeVotes;
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

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public CommentType getCommentType() {
		return commentType;
	}

	public void setCommentType(CommentType commentType) {
		this.commentType = commentType;
	}
}
