package com.livae.ff.api.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfNotNull;
import com.googlecode.objectify.condition.IfTrue;
import com.livae.ff.common.Constants.CommentVoteType;
import com.livae.ff.common.Constants.FlagReason;
import com.livae.ff.common.Constants.UserMark;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nonnull;

import static com.livae.ff.api.OfyService.ofy;

@Entity
public class Comment implements Serializable {

	@Id
	private Long id;

	@Index
	private Long conversationId;

	private Long aliasId;

	private String alias;

	@Index
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Long userId;

	@Index
	private Date date;

	private String comment;

	private Integer agreeVotes;

	private Integer disagreeVotes;

	@Ignore
	private CommentVoteType voteType;

	@Ignore
	private CommentVoteType userVoteType;

	@Ignore
	private Boolean isMe;

	@Index(IfTrue.class)
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Boolean deleted;

	@Index(IfNotNull.class)
	private Integer timesFlagged;

	@Index(IfNotNull.class)
	private Date lastTimeFlagged;

	private Integer[] timesFlaggedType;

	private UserMark userMark;

	public Comment() {
	}

	public Comment(@Nonnull Long conversationId, @Nonnull Long userId, @Nonnull String comment) {
		this.conversationId = conversationId;
		this.userId = userId;
		this.comment = comment;
		date = new Date();
		agreeVotes = 0;
		disagreeVotes = 0;
		userVoteType = null;
		deleted = false;
	}

	public Comment(@Nonnull Long conversationId, @Nonnull Long userId, @Nonnull String comment,
				   @Nonnull Long aliasId, @Nonnull String alias) {
		this(conversationId, userId, comment);
		this.aliasId = aliasId;
		this.alias = alias;
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

	public Long getConversationId() {
		return conversationId;
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}

	public Long getAliasId() {
		return aliasId;
	}

	public void setAliasId(Long aliasId) {
		this.aliasId = aliasId;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
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

	public Integer getAgreeVotes() {
		return agreeVotes;
	}

	public void setAgreeVotes(Integer agreeVotes) {
		this.agreeVotes = agreeVotes;
	}

	public Integer getDisagreeVotes() {
		return disagreeVotes;
	}

	public void setDisagreeVotes(Integer disagreeVotes) {
		this.disagreeVotes = disagreeVotes;
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

	public Integer getTimesFlagged() {
		return timesFlagged;
	}

	public void setTimesFlagged(Integer timesFlagged) {
		this.timesFlagged = timesFlagged;
	}

	public Date getLastTimeFlagged() {
		return lastTimeFlagged;
	}

	public void setLastTimeFlagged(Date lastTimeFlagged) {
		this.lastTimeFlagged = lastTimeFlagged;
	}

	public void flag(FlagReason flagReason) {
		lastTimeFlagged = new Date();
		if (timesFlagged == null) {
			timesFlagged = 0;
			timesFlaggedType = new Integer[FlagReason.values().length];
			for (int i = 0; i < timesFlaggedType.length; i++) {
				timesFlaggedType[i] = 0;
			}
		}
		timesFlagged++;
		timesFlaggedType[flagReason.ordinal()]++;
	}

	public Integer[] getTimesFlaggedType() {
		return timesFlaggedType;
	}

	public UserMark getUserMark() {
		return userMark;
	}

	public void setUserMark(UserMark userMark) {
		this.userMark = userMark;
	}
}
