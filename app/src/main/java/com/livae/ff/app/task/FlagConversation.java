package com.livae.ff.app.task;

public class FlagConversation {

	private Long conversationId;

	private Long time;

	public FlagConversation() {
	}

	public FlagConversation(Long conversationId, Long time) {

		this.conversationId = conversationId;
		this.time = time;
	}

	public Long getConversationId() {
		return conversationId;
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}
}
