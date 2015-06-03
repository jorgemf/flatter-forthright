package com.livae.ff.app.task;

import com.livae.ff.common.Constants.ChatType;

public class QueryComments extends QueryParam {

	private Long id;

	private ChatType chatType;

	public QueryComments(Long id, ChatType chatType, Integer limit) {
		super(limit);
		this.id = id;
		this.chatType = chatType;
	}

	public QueryComments(Long id, ChatType chatType) {
		this.id = id;
		this.chatType = chatType;
	}

	public Long getId() {
		return id;
	}

	public ChatType getChatType() {
		return chatType;
	}

	@Override
	public String toString() {
		return "[id: " + id + "] " + "[commentType: " + chatType + "] " + super.toString();
	}
}
