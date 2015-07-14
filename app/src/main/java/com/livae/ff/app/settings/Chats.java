package com.livae.ff.app.settings;

import android.content.SharedPreferences;

public class Chats {

	private static final String USER_IMAGE_URI = "ff.user.image_uri";

	private static final String USER_DISPLAY_NAME = "ff.user.display_name";

	private static final String USER_ANONYMOUS_NAME = "ff.user.anonymous_name";

	private static final String USER_CHAT_FLATTER_ID = "ff.chat.flatter.id";

	private static final String USER_CHAT_FORTHRIGHT_ID = "ff.chat.forthright.id";

	private static final String USER_CHAT_FLATTER_UNREAD = "ff.chat.flatter.unread";

	private static final String USER_CHAT_FORTHRIGHT_UNREAD = "ff.chat.forthright.unread";

	private SharedPreferences prefs;

	private String displayName;

	private String anonymousName;

	private String imageUri;

	private Long chatFlatterId;

	private Long chatForthrightId;

	private int chatFlatterUnread;

	private int chatForthrightUnread;

	public Chats(SharedPreferences prefs) {
		this.prefs = prefs;
		load(prefs);
	}

	private void load(SharedPreferences prefs) {
		imageUri = prefs.getString(USER_IMAGE_URI, null);
		displayName = prefs.getString(USER_DISPLAY_NAME, null);
		anonymousName = prefs.getString(USER_ANONYMOUS_NAME, null);
		chatFlatterId = prefs.getLong(USER_CHAT_FLATTER_ID, 0L);
		if (chatFlatterId == 0) {
			chatFlatterId = null;
		}
		chatForthrightId = prefs.getLong(USER_CHAT_FORTHRIGHT_ID, 0L);
		if (chatForthrightId == 0) {
			chatForthrightId = null;
		}
		chatFlatterUnread = prefs.getInt(USER_CHAT_FLATTER_UNREAD, 0);
		chatForthrightUnread = prefs.getInt(USER_CHAT_FLATTER_UNREAD, 0);
	}

	public String getUserImageUri() {
		return imageUri;
	}

	public void setUserImageUri(String imageUri) {
		this.imageUri = imageUri;
		prefs.edit().putString(USER_IMAGE_URI, imageUri).apply();
	}

	public String getUserDisplayName() {
		return displayName;
	}

	public void setUserDisplayName(String displayName) {
		this.displayName = displayName;
		prefs.edit().putString(USER_DISPLAY_NAME, displayName).apply();
	}

	public String getUserAnonymousName() {
		return anonymousName;
	}

	public void setUserAnonymousName(String anonymousName) {
		this.anonymousName = anonymousName;
		prefs.edit().putString(USER_ANONYMOUS_NAME, anonymousName).apply();
	}

	public Long getChatFlatterId() {
		return chatFlatterId;
	}

	public void setChatFlatterId(Long chatFlatterId) {
		this.chatFlatterId = chatFlatterId;
		if (chatFlatterId == null) {
			prefs.edit().remove(USER_CHAT_FLATTER_ID).apply();
		} else {
			prefs.edit().putLong(USER_CHAT_FLATTER_ID, chatFlatterId).apply();
		}
	}

	public Long getChatForthrightId() {
		return chatForthrightId;
	}

	public void setChatForthrightId(Long chatForthrightId) {
		this.chatForthrightId = chatForthrightId;
		if (chatForthrightId == null) {
			prefs.edit().remove(USER_CHAT_FORTHRIGHT_ID).apply();
		} else {
			prefs.edit().putLong(USER_CHAT_FORTHRIGHT_ID, chatForthrightId).apply();
		}
	}

	public int getChatFlatterUnread() {
		return chatFlatterUnread;
	}

	public void setChatFlatterUnread(int chatFlatterUnread) {
		this.chatFlatterUnread = chatFlatterUnread;
		prefs.edit().putInt(USER_CHAT_FLATTER_UNREAD, chatFlatterUnread).apply();
	}

	public int getChatForthrightUnread() {
		return chatForthrightUnread;
	}

	public void setChatForthrightUnread(int chatForthrightUnread) {
		this.chatForthrightUnread = chatForthrightUnread;
		prefs.edit().putInt(USER_CHAT_FORTHRIGHT_UNREAD, chatForthrightUnread).apply();
	}

	public void increaseChatFlatterUnread() {
		chatFlatterUnread++;
		prefs.edit().putInt(USER_CHAT_FLATTER_UNREAD, chatFlatterUnread).apply();
	}

	public void increaseChatForthrightUnread() {
		chatForthrightUnread++;
		prefs.edit().putInt(USER_CHAT_FORTHRIGHT_UNREAD, chatForthrightUnread).apply();
	}

	public String toString() {
		return "[imageUri = " + imageUri + "] " +
			   "[displayName = " + displayName + "] " +
			   "[anonymousName = " + anonymousName + "] ";
	}

}
