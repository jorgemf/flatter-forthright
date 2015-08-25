package com.livae.ff.app.task;

import android.support.annotation.NonNull;

import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.LifeCycle;

public class TaskConversationJoin extends NetworkAsyncTask<LifeCycle, Long, Void> {

	public TaskConversationJoin(@NonNull LifeCycle lifeCycle) {
		super(lifeCycle);
	}

	@Override
	protected Void doInBackground(Long conversationId)
	  throws Exception {
		if (!BuildConfig.TEST) {
			API.endpoint().joinConversation(conversationId).execute();
		}
		return null;
	}

}
