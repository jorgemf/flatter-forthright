package com.livae.ff.app.api;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.livae.ff.api.ff.model.CollectionResponseComment;
import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.provider.DataProvider;
import com.livae.ff.app.sql.Table;

import java.util.ArrayList;
import java.util.List;

public class Model {

	private static final String LOG_TAG = "MODEL";

	private List<ContentValues> conversationsList;

	private List<ContentValues> commentsList;

	private List<ContentValues> phonesList;

	private Context context;

	public Model(Context applicationContext) {
		this.context = applicationContext;
		commentsList = new ArrayList<>();
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
		}
		ContentResolver contentResolver = context.getContentResolver();
		if (phonesList.size() > 0) {
			ContentValues contentValues = new ContentValues();
			contentValues.put(Table.LocalUser.ACCEPTS_PRIVATE_ANONYMOUS, false);
			contentResolver.update(DataProvider.getUriContacts(), contentValues, null, null);
			contentResolver.bulkInsert(DataProvider.getUriComments(),
									   phonesList.toArray(new ContentValues[phonesList.size()]));
			phonesList.clear();
		}
		if (conversationsList.size() > 0) {
			contentResolver.bulkInsert(DataProvider.getUriComments(),
									   conversationsList.toArray(new ContentValues[conversationsList
																					 .size()]));
			conversationsList.clear();
		}
		if (commentsList.size() > 0) {
			contentResolver.bulkInsert(DataProvider.getUriComments(),
									   commentsList.toArray(new ContentValues[commentsList
																				.size()]));
			commentsList.clear();
		}
	}

	public synchronized void parse(Comment comment) {
		ContentValues val = new ContentValues();

		val.put(Table.Comment.ID, comment.getId());
		val.put(Table.Comment.CONVERSATION_ID, comment.getConversationId());
		val.put(Table.Comment.USER_ANONYMOUS_ID, comment.getUserAnonymousId());
		val.put(Table.Comment.USER_ALIAS, comment.getUserAlias());
		val.put(Table.Comment.AGREE_VOTES, comment.getAgreeVotes());
		val.put(Table.Comment.DISAGREE_VOTES, comment.getDisagreeVotes());
		val.put(Table.Comment.DATE, comment.getDate().getValue());
		val.put(Table.Comment.PHONE, comment.getPhone());
		val.put(Table.Comment.VOTE_TYPE, comment.getVoteType());
		val.put(Table.Comment.USER_VOTE_TYPE, comment.getUserVoteType());
		val.put(Table.Comment.COMMENT, comment.getComment());

		commentsList.add(val);
	}

	public synchronized void parse(CollectionResponseComment comments) {
		if (comments != null && comments.getItems() != null) {
			for (Comment comment : comments.getItems()) {
				parse(comment);
			}
		}
	}

	public synchronized void parse(Conversation conversation) {
		ContentValues val = new ContentValues();

		val.put(Table.Conversation.ID, conversation.getId());
		val.put(Table.Conversation.TYPE, conversation.getType());
		val.put(Table.Conversation.PHONE, conversation.getPhone());
		val.put(Table.Conversation.CONVERSATION_ALIAS, conversation.getConversationAlias());

		commentsList.add(val);
	}

	public synchronized void parse(CollectionResponseConversations conversations) {
		if (conversations != null && conversations.getItems() != null) {
			for (Conversation conversation : conversations.getItems()) {
				parse(conversation);
			}
		}
	}

	public synchronized void parse(Long phone) {
		ContentValues val = new ContentValues();

		val.put(Table.LocalUser.PHONE, phone);
		val.put(Table.LocalUser.ACCEPTS_PRIVATE_ANONYMOUS, true);

		commentsList.add(val);
	}

	public synchronized void parse(CollectionResponsePhones phones) {
		if (phones != null && phones.getItems() != null) {
			for (Long phone : phones.getItems()) {
				parse(phone);
			}
		}
	}

}
