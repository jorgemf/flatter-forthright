package com.livae.ff.app.task;

import android.content.ContentResolver;
import android.content.ContentValues;

import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.provider.ConversationsProvider;
import com.livae.ff.app.sql.Table;

public class TaskConversationJoin extends NetworkAsyncTask<Long, Void> {

	@Override
	protected Void doInBackground(Long conversationId) throws Exception {
		API.endpoint().joinConversation(conversationId).execute();
		ContentResolver contentResolver = Application.getContext().getContentResolver();
		ContentValues values = new ContentValues();
		values.put(Table.Conversation.LAST_ACCESS, System.currentTimeMillis());
		contentResolver.update(ConversationsProvider.getUriConversation(conversationId), values,
							   null, null);
		return null;
	}

}