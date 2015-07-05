package com.livae.ff.app.task;

import android.support.annotation.NonNull;

import com.livae.ff.common.Constants.ChatType;

public class ConversationParams {

	private ChatType chatType;

	private Long phoneNumber;

	private String roomName;

	public ConversationParams(@NonNull ChatType chatType, @NonNull Long phoneNumber) {
		this.chatType = chatType;
		this.phoneNumber = phoneNumber;
	}

	public ConversationParams(@NonNull ChatType chatType, @NonNull Long phoneNumber,
							  String roomName) {
		this.chatType = chatType;
		this.phoneNumber = phoneNumber;
		this.roomName = roomName;
	}

	@NonNull
	public ChatType getChatType() {
		return chatType;
	}

	public void setChatType(@NonNull ChatType chatType) {
		this.chatType = chatType;
	}

	@NonNull
	public Long getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(@NonNull Long phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
}
