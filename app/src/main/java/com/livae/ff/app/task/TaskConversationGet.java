package com.livae.ff.app.task;

import com.livae.ff.api.ff.model.Conversation;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;

@Deprecated
public class TaskConversationGet extends NetworkAsyncTask<Long, Conversation> {

	@Override
	protected Conversation doInBackground(Long conversationId)
	  throws Exception {
		Conversation conversation = API.endpoint().getConversation(conversationId).execute();
		Model model = new Model(Application.getContext());
		model.parse(conversation);
		model.save();
		return conversation;
	}

}
