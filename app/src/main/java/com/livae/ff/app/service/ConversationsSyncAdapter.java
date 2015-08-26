package com.livae.ff.app.service;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.livae.ff.api.ff.Ff;
import com.livae.ff.api.ff.model.Comment;
import com.livae.ff.api.ff.model.Text;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.sql.Table;
import com.livae.ff.app.utils.DeviceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConversationsSyncAdapter extends AbstractThreadedSyncAdapter {

	private static final String LOG_TAG = "COMMENTS_SYNC_SERVICE";

	private ContentResolver contentResolver;

	public ConversationsSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		contentResolver = context.getContentResolver();
	}

	public ConversationsSyncAdapter(Context context,
									boolean autoInitialize,
									boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
		contentResolver = context.getContentResolver();
	}

	@Override
	public void onPerformSync(Account account,
							  Bundle extras,
							  String authority,
							  ContentProviderClient provider,
							  SyncResult syncResult) {
		Log.i(LOG_TAG, "Starting comments synchronization");
		List<CommentSync> comments = getCommentsSync();
		for (CommentSync comment : comments) {
			processComment(comment);
		}
		Log.i(LOG_TAG, "Finished comments synchronization");
	}

	private void processComment(CommentSync comment) {
		if (DeviceUtils.isNetworkAvailable(Application.getContext())) {
			Text text = new Text();
			text.setText(comment.comment);
			Ff.ApiEndpoint.PostComment request;
			Comment commentPosted;
			try {
				request = API.endpoint().postComment(comment.conversationId, text);
				request.setAlias(comment.alias);
				commentPosted = request.execute();
				commentPosted.setIsMe(true);
				Model model = Application.model();
				model.parse(commentPosted, comment.date);
				model.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
			final Uri uriCommentSync = ConversationsProvider.getUriCommentSync(comment.id);
			contentResolver.delete(uriCommentSync, null, null);
			Uri uriConversation = ConversationsProvider.getUriConversation(comment.conversationId);
			contentResolver.notifyChange(uriConversation, null);
		}
	}

	private List<CommentSync> getCommentsSync() {
		List<CommentSync> comments = new ArrayList<CommentSync>();
		Cursor cursor =
		  contentResolver.query(ConversationsProvider.getUriCommentsSync(), null, null, null,
								Table.CommentSync.DATE);
		if (cursor.moveToFirst()) {
			int iId = cursor.getColumnIndex(Table.CommentSync.ID);
			int iConversationId = cursor.getColumnIndex(Table.CommentSync.CONVERSATION_ID);
			int iAlias = cursor.getColumnIndex(Table.CommentSync.USER_ALIAS);
			int iComment = cursor.getColumnIndex(Table.CommentSync.COMMENT);
			int iDate = cursor.getColumnIndex(Table.CommentSync.DATE);
			do {
				CommentSync comment = new CommentSync();
				comment.id = cursor.getLong(iId);
				comment.conversationId = cursor.getLong(iConversationId);
				comment.comment = cursor.getString(iComment);
				comment.alias = cursor.getString(iAlias);
				comment.date = cursor.getLong(iDate);
				comments.add(comment);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return comments;
	}

	class CommentSync {

		long id;

		long conversationId;

		String comment;

		String alias;

		long date;
	}

}
