package com.livae.ff.app.model;

public class ChatPublicModel extends UserModel {

	public Long conversationId;

	public String roomName;

	public void clear() {
		super.clear();
		conversationId = null;
		roomName = null;
	}
}
