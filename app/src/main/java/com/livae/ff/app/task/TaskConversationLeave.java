package com.livae.ff.app.task;

import android.support.annotation.NonNull;

import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.LifeCycle;

public class TaskConversationLeave extends NetworkAsyncTask<LifeCycle, Long, Void> {

	public TaskConversationLeave(@NonNull LifeCycle lifeCycle) {
		super(lifeCycle);
	}

	@Override
	protected Void doInBackground(Long conversationId)
	  throws Exception {
		API.endpoint().leaveConversation(conversationId).execute();
		return null;
	}

}
