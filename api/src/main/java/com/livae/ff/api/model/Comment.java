package com.livae.ff.api.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfTrue;
import com.livae.ff.common.Constants.ChatType;
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

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Long userId;

	@Index
	private Long phone;

	@Index
	private Date date;

	@Index
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private ChatType chatType;

	private String comment;

	private Integer agreeVotes;

	private Integer disagreeVotes;

	@Index
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Integer votes;

	@Ignore
	private CommentVoteType voteType;

	private CommentVoteType userVoteType;

	@Ignore
	private Boolean isMe;

	@Index(IfTrue.class)
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Boolean deleted;

	public Comment() {
	}

	public Comment(@Nonnull ChatType chatType, @Nonnull Long phone, @Nonnull Long user,
				   @Nonnull Long userId, @Nonnull String comment) {
		this.chatType = chatType;
		this.user = user;
		this.userId = userId;
		this.phone = phone;
		this.comment = comment;
		date = new Date();
		agreeVotes = 0;
		disagreeVotes = 0;
		votes = 0;
		userVoteType = null;
		deleted = false;
		isMe = false;
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

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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
	}

	public Integer getDisagreeVotes() {
		return disagreeVotes;
	}

	public void setDisagreeVotes(Integer disagreeVotes) {
		this.disagreeVotes = disagreeVotes;
		this.votes = this.agreeVotes - this.disagreeVotes;
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

	public ChatType getChatType() {
		return chatType;
	}

	public void setChatType(ChatType chatType) {
		this.chatType = chatType;
	}

	public Boolean getIsMe() {
		return isMe;
	}

	public void setIsMe(Boolean isMe) {
		this.isMe = isMe;
	}

	public CommentVoteType getUserVoteType() {
		return userVoteType;
	}

	public void setUserVoteType(CommentVoteType userVoteType) {
		this.userVoteType = userVoteType;
	}
}
