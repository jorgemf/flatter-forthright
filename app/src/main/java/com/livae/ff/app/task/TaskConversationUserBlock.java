package com.livae.ff.app.task;

import android.support.annotation.NonNull;

import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.LifeCycle;

public class TaskConversationUserBlock extends NetworkAsyncTask<LifeCycle, FlagConversation, Void> {

	public TaskConversationUserBlock(@NonNull LifeCycle lifeCycle) {
		super(lifeCycle);
	}

	@Override
	protected Void doInBackground(FlagConversation conversation)
	  throws Exception {
		API.endpoint()
		   .conversationBlockUser(conversation.getConversationId(), conversation.getTime())
		   .execute();
		return null;
	}

}
