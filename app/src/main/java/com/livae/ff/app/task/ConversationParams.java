package com.livae.ff.app.task;

import com.livae.ff.common.Constants.ChatType;

import javax.annotation.Nonnull;

public class ConversationParams {

	private ChatType chatType;

	private Long phoneNumber;

	private String roomName;

	public ConversationParams(@Nonnull ChatType chatType, @Nonnull Long phoneNumber) {
		this.chatType = chatType;
		this.phoneNumber = phoneNumber;
	}

	public ConversationParams(@Nonnull ChatType chatType, @Nonnull Long phoneNumber,
							  String roomName) {
		this.chatType = chatType;
		this.phoneNumber = phoneNumber;
		this.roomName = roomName;
	}

	@Nonnull
	public ChatType getChatType() {
		return chatType;
	}

	public void setChatType(@Nonnull ChatType chatType) {
		this.chatType = chatType;
	}

	@Nonnull
	public Long getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(@Nonnull Long phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
}
