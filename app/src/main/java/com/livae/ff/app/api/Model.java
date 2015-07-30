package com.livae.ff.app.api;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.api.client.util.DateTime;
import com.livae.ff.api.ff.model.CollectionResponseComment;
import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.api.ff.model.Conversation;
import com.livae.ff.api.ff.model.Numbers;
import com.livae.ff.app.AppUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.provider.ContactsProvider;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.sql.Table;
import com.livae.ff.common.Constants;
import com.livae.ff.common.Constants.FlagReason;
import com.livae.ff.common.model.Notification;
import com.livae.ff.common.model.NotificationComment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class Model {

	private static final String LOG_TAG = "MODEL";

	private List<ContentValues> conversationsList;

	private List<ContentValues> commentsList;

	private List<ContentValues> commentsSyncList;

	private List<ContentValues> phonesList;

	private Context context;

	public Model(Context applicationContext) {
		this.context = applicationContext;
		conversationsList = new ArrayList<>();
		commentsList = new ArrayList<>();
		commentsSyncList = new ArrayList<>();
		phonesList = new ArrayList<>();
	}

	public synchronized void save() {
		if (BuildConfig.DEBUG) {
			if (phonesList.size() > 0) {
				Log.v(LOG_TAG, "PHONES");
				for (ContentValues value : phonesList) {
					Log.v(LOG_TAG, value.toString());
				}
			}
			if (conversationsList.size() > 0) {
				Log.v(LOG_TAG, "CONVERSATIONS");
				for (ContentValues value : conversationsList) {
					Log.v(LOG_TAG, value.toString());
				}
			}
			if (commentsList.size() > 0) {
				Log.v(LOG_TAG, "COMMENTS");
				for (ContentValues value : commentsList) {
					Log.v(LOG_TAG, value.toString());
				}
			}
			if (commentsSyncList.size() > 0) {
				Log.v(LOG_TAG, "COMMENTS_SYNC");
				for (ContentValues value : commentsSyncList) {
					Log.v(LOG_TAG, value.toString());
				}
			}
		}
		ContentResolver contentResolver = context.getContentResolver();
		if (phonesList.size() > 0) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(Table.LocalUser.ACCEPTS_PRIVATE, false);
			contentResolver.update(ContactsProvider.getUriContacts(), contentValues, null, null);
			contentResolver.bulkInsert(ContactsProvider.getUriContacts(),
									   phonesList.toArray(new ContentValues[phonesList.size()]));
			phonesList.clear();
		}
		if (conversationsList.size() > 0) {
			for (ContentValues values : conversationsList) {
				Long id = values.getAsLong(Table.Conversation.ID);
				Uri uriConversation = ConversationsProvider.getUriConversation(id);
				contentResolver.insert(uriConversation, values);
			}
			conversationsList.clear();
		}
		if (commentsList.size() > 0) {
			contentResolver.bulkInsert(ConversationsProvider.getUriComments(),
									   commentsList.toArray(new ContentValues[commentsList
																				.size()]));
			commentsList.clear();
		}
		if (commentsSyncList.size() > 0) {
			contentResolver.bulkInsert(ConversationsProvider.getUriCommentsSync(),
									   commentsSyncList.toArray(new ContentValues[commentsSyncList
																					.size()]));
			commentsSyncList.clear();
		}
	}

	public void parse(Comment comment) {
		parse(comment, false);
	}

	public synchronized void parse(Comment comment, boolean forSyncing) {
		ContentValues val = new ContentValues();
		Long date;
		Long conversationId = comment.getConversationId();
		String commentText = comment.getComment();
		if (forSyncing) {
			val.put(Table.CommentSync.CONVERSATION_ID, conversationId);
			date = System.currentTimeMillis();
			val.put(Table.CommentSync.DATE, date);
			val.put(Table.CommentSync.COMMENT, commentText);
			val.put(Table.CommentSync.USER_ALIAS, comment.getAlias());
			commentsSyncList.add(val);
		} else {
			val.put(Table.Comment.ID, comment.getId());
			val.put(Table.Comment.CONVERSATION_ID, conversationId);
			val.put(Table.Comment.USER_ANONYMOUS_ID, comment.getAliasId());
			val.put(Table.Comment.USER_ALIAS, comment.getAlias());
			val.put(Table.Comment.AGREE_VOTES, comment.getAgreeVotes());
			val.put(Table.Comment.DISAGREE_VOTES, comment.getDisagreeVotes());
			date = comment.getDate().getValue();
			val.put(Table.Comment.DATE, date);
			val.put(Table.Comment.IS_ME, comment.getIsMe());
			val.put(Table.Comment.VOTE_TYPE, comment.getVoteType());
			val.put(Table.Comment.USER_VOTE_TYPE, comment.getUserVoteType());
			val.put(Table.Comment.COMMENT, commentText);
			val.put(Table.Comment.USER_MARK, comment.getUserMark());
			val.put(Table.Comment.TIMES_FLAGGED, comment.getTimesFlagged());
			final List<Integer> flaggedTypeList = comment.getTimesFlaggedType();
			int[] flaggedType = new int[FlagReason.values().length];
			if (flaggedTypeList != null && flaggedTypeList.size() >= flaggedType.length) {
				for (int i = 0; i < flaggedType.length; i++) {
					flaggedType[i] = flaggedTypeList.get(i);
				}
			}
			val.put(Table.Comment.TIMES_FLAGGED_ABUSE, flaggedType[FlagReason.ABUSE.ordinal()]);
			val.put(Table.Comment.TIMES_FLAGGED_INSULT, flaggedType[FlagReason.INSULT.ordinal()]);
			val.put(Table.Comment.TIMES_FLAGGED_LIE, flaggedType[FlagReason.LIE.ordinal()]);
			val.put(Table.Comment.TIMES_FLAGGED_OTHER, flaggedType[FlagReason.OTHER.ordinal()]);
			commentsList.add(val);
		}
		parse(conversationId, date, commentText);
	}

	public synchronized void parse(CollectionResponseComment comments) {
		if (comments != null && comments.getItems() != null) {
			for (Comment comment : comments.getItems()) {
				parse(comment);
			}
		}
	}

	@Deprecated
	public void parse(Conversation conversation) {
		parse(conversation, null, null);
	}

	public synchronized void parse(@Nonnull Long conversationId, @Nonnull Long lastMessageDate,
								   @Nonnull String lastMessage) {
		ContentValues val = new ContentValues();
		val.put(Table.Conversation.ID, conversationId);
		val.put(Table.Conversation.LAST_MESSAGE_DATE, lastMessageDate);
		val.put(Table.Conversation.LAST_MESSAGE, lastMessage);
		conversationsList.add(val);
	}

	public synchronized void parse(Conversation conversation, Long lastMessageDate,
								   String lastMessage) {
		ContentValues val = new ContentValues();

		val.put(Table.Conversation.ID, conversation.getId());
		val.put(Table.Conversation.TYPE, conversation.getType());
		final Long conversationPhone = conversation.getPhone();
		if (conversationPhone != null) {
			val.put(Table.Conversation.PHONE, conversationPhone);
		}
		final String roomName = conversation.getAlias();
		if (roomName != null) {
			val.put(Table.Conversation.ROOM_NAME, roomName);
		}
		final Long aliasId = conversation.getAliasId();
		if (aliasId != null) {
			val.put(Table.Conversation.ALIAS_ID, aliasId);
		}
		if (lastMessageDate != null) {
			val.put(Table.Conversation.LAST_MESSAGE_DATE, lastMessageDate);
		}
		if (lastMessage != null) {
			val.put(Table.Conversation.LAST_MESSAGE, lastMessage);
		}

		conversationsList.add(val);
	}

	public synchronized void parse(Long phone) {
		ContentValues val = new ContentValues();

		val.put(Table.LocalUser.PHONE, phone);
		val.put(Table.LocalUser.ACCEPTS_PRIVATE, true);

		phonesList.add(val);
	}

	public void parseBlocked(Numbers blockedNumbers) {
		for (Long phone : blockedNumbers.getNumbers()) {
			parseBlocked(phone, true);
		}
	}

	public void parseBlocked(Long phone, boolean blocked) {
		ContentValues val = new ContentValues();

		val.put(Table.LocalUser.PHONE, phone);
		val.put(Table.LocalUser.BLOCKED, blocked);

		phonesList.add(val);
	}

	public void parse(Notification notification) {
		if (notification instanceof NotificationComment) {
			NotificationComment nc = (NotificationComment) notification;
			Comment comment = new Comment();
			String commentText = nc.getComment();
			comment.setComment(commentText);
			Long conversationId = nc.getConversationId();
			comment.setConversationId(conversationId);
			comment.setUserMark(nc.getUserMark());
			comment.setDate(new DateTime(nc.getDate()));
			comment.setIsMe(nc.getIsMe());
			comment.setId(nc.getId());
			try {
				Constants.ChatType chatType = Constants.ChatType.valueOf(nc.getConversationType());
				Conversation conversation = new Conversation();
				conversation.setType(nc.getConversationType());
				conversation.setId(conversationId);
				switch (chatType) {
					case FORTHRIGHT:
					case FLATTER:
						comment.setAlias(nc.getAlias());
						comment.setAliasId(nc.getAliasId());
						Long conversationPhone = nc.getConversationUserId();
						AppUser appUser = Application.appUser();
						if (appUser.getUserPhone().equals(conversationPhone)) {
							switch (chatType) {
								case FORTHRIGHT:
									appUser.getChats().setChatForthrightId(conversationId);
									break;
								case FLATTER:
									appUser.getChats().setChatFlatterId(conversationId);
									break;
							}
						}
						break;
					case PRIVATE_ANONYMOUS:
						conversation.setAlias(nc.getAlias());
						conversation.setAliasId(nc.getAliasId());
						break;
					case SECRET:
					case PRIVATE:
						if (!nc.getIsMe()) {
							conversation.setPhone(nc.getUserId());
						}
						break;
				}
				parse(conversation, comment.getDate().getValue(), commentText);
			} catch (IllegalArgumentException ignore) {
				ignore.printStackTrace();
			}
			parse(comment);
		}
	}
}
