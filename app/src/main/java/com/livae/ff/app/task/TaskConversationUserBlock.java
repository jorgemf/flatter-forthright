package com.livae.ff.app.task;

import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskConversationUserBlock extends NetworkAsyncTask<FlagConversation, Void> {

	@Override
	protected Void doInBackground(FlagConversation conversation) throws Exception {
		API.endpoint().conversationBlockUser(conversation.getConversationId(),
											 conversation.getTime()).execute();
		return null;
	}

}