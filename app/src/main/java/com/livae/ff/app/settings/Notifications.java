package com.livae.ff.app.settings;

import android.content.SharedPreferences;

@SuppressWarnings("UnusedDeclaration")
public class Notifications {

	private static final String NOTIFICATIONS_COMMENTS_CHAT = "ff.notifications.comments.chats";

	private static final String NOTIFICATIONS_COMMENTS_FLATTERED_ME =
	  "ff.notifications.comments" + ".flattered.me";

	private static final String NOTIFICATIONS_COMMENTS_FORTHRIGHT_ME =
	  "ff.notifications.comments" + ".forthright.me";

	private static final String NOTIFICATIONS_IMPORTANT = "apphunt.notifications.important";

	private boolean commentsChat;

	private boolean commentsFlatteredMe;

	private boolean commentsForthrightMe;

	private boolean important;

	private SharedPreferences prefs;

	public Notifications(SharedPreferences prefs) {
		this.prefs = prefs;
		load(prefs);
	}

	private void load(SharedPreferences prefs) {
		commentsChat = prefs.getBoolean(NOTIFICATIONS_COMMENTS_CHAT, true);
		commentsFlatteredMe = prefs.getBoolean(NOTIFICATIONS_COMMENTS_FLATTERED_ME, true);
		commentsForthrightMe = prefs.getBoolean(NOTIFICATIONS_COMMENTS_FORTHRIGHT_ME, true);
		important = prefs.getBoolean(NOTIFICATIONS_IMPORTANT, true);
	}

	public boolean isCommentsChat() {
		return commentsChat;
	}

	public void setCommentsChat(boolean commentsChat) {
		this.commentsChat = commentsChat;
		prefs.edit().putBoolean(NOTIFICATIONS_COMMENTS_CHAT, commentsChat).apply();
	}

	public boolean isCommentsFlatteredMe() {
		return commentsFlatteredMe;
	}

	public void setCommentsFlatteredMe(boolean commentsFlatteredMe) {
		this.commentsFlatteredMe = commentsFlatteredMe;
		prefs.edit().putBoolean(NOTIFICATIONS_COMMENTS_FLATTERED_ME, commentsFlatteredMe).apply();
	}

	public boolean isCommentsForthrightMe() {
		return commentsForthrightMe;
	}

	public void setCommentsForthrightMe(boolean commentsForthrightMe) {
		this.commentsForthrightMe = commentsForthrightMe;
		prefs.edit().putBoolean(NOTIFICATIONS_COMMENTS_FORTHRIGHT_ME, commentsForthrightMe)
			 .apply();
	}

	public boolean getImportant() {
		return important;
	}

	public void setImportant(boolean important) {
		this.important = important;
		prefs.edit().putBoolean(NOTIFICATIONS_IMPORTANT, important).apply();
	}

	public String toString() {
		return "[commentsChat = " + commentsChat + "] " +
			   "[commentsFlatteredMe = " + commentsFlatteredMe + "] " +
			   "[commentsForthrightMe = " + commentsForthrightMe + "] " +
			   "[important = " + important + "] ";
	}

}
