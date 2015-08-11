package com.livae.ff.app.task;

import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskConversationJoin extends NetworkAsyncTask<Long, Void> {

	@Override
	protected Void doInBackground(Long conversationId)
	  throws Exception {
		if (!BuildConfig.TEST) {
			API.endpoint().joinConversation(conversationId).execute();
		}
		return null;
	}

}
