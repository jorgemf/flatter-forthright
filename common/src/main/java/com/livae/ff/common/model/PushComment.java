package com.livae.ff.common.model;

import com.livae.ff.common.Constants;

import java.util.Date;

public class PushComment {

	private Long id;

	private Long conversationId;

	private Long aliasId;

	private String alias;

	private Date date;

	private String comment;

	private Boolean isMe;

	private Constants.UserMark userMark;

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

	public Boolean getIsMe() {
		return isMe;
	}

	public void setIsMe(Boolean isMe) {
		this.isMe = isMe;
	}

	public Constants.UserMark getUserMark() {
		return userMark;
	}

	public void setUserMark(Constants.UserMark userMark) {
		this.userMark = userMark;
	}
}
