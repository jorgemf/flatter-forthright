package com.livae.ff.app.task;

import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskConversationUserBlock extends NetworkAsyncTask<Long, Void> {

	@Override
	protected Void doInBackground(Long conversationId) throws Exception {
		API.endpoint().conversationBlockUser(conversationId).execute();
		// TODO
		return null;
	}

}