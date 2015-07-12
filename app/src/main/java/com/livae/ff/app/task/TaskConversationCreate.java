package com.livae.ff.app.task;

import com.livae.ff.api.ff.Ff.ApiEndpoint.GetPhoneConversation;
import com.livae.ff.api.ff.model.Conversation;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskConversationCreate extends NetworkAsyncTask<ConversationParams, Conversation> {

	@Override
	protected Conversation doInBackground(ConversationParams conversation) throws Exception {
		GetPhoneConversation request;
		request = API.endpoint().getPhoneConversation(conversation.getPhoneNumber(),
													  conversation.getChatType().name());
		if (conversation.getRoomName() != null) {
			request.setRoomName(conversation.getRoomName());
		}
		Conversation c = request.execute();
		c.setPhone(conversation.getPhoneNumber());
		Model model = new Model(Application.getContext());
		model.parse(c, System.currentTimeMillis(), null);
		model.save();
		return c;
	}

}