package com.livae.ff.common.model;

import com.livae.ff.common.Constants;

import java.util.Date;

public class NotificationComment implements Notification {

	private Long id;

	private Long conversationId;

	private Long conversationUserId;

	private Long userId;

	private Long aliasId;

	private String alias;

	private Date date;

	private String comment;

	private Boolean isMe;

	private String userMark;

	private String conversationType;

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

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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

	public String getUserMark() {
		return userMark;
	}

	public void setUserMark(String userMark) {
		this.userMark = userMark;
	}

	public String getConversationType() {
		return conversationType;
	}

	public void setConversationType(String conversationType) {
		this.conversationType = conversationType;
	}

	public Constants.PushNotificationType getType() {
		return Constants.PushNotificationType.COMMENT;
	}

	public Long getConversationUserId() {
		return conversationUserId;
	}

	public void setConversationUserId(Long conversationUserId) {
		this.conversationUserId = conversationUserId;
	}
}
