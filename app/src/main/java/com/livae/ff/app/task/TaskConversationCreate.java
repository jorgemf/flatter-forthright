package com.livae.ff.app.task;

import com.livae.ff.api.ff.Ff.ApiEndpoint.GetPhoneConversation;
import com.livae.ff.api.ff.model.Conversation;
import com.livae.ff.app.Application;
import com.livae.ff.app.BuildConfig;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskConversationCreate extends NetworkAsyncTask<ConversationParams, Conversation> {

	private static long FAKE_IDS = 3000L;

	@Override
	protected Conversation doInBackground(ConversationParams conversation)
	  throws Exception {
		Conversation c;
		if (!BuildConfig.TEST) {
			GetPhoneConversation request;
			request = API.endpoint()
						 .getPhoneConversation(conversation.getPhoneNumber(),
											   conversation.getChatType().name());
			if (conversation.getRoomName() != null) {
				request.setRoomName(conversation.getRoomName());
			}
			c = request.execute();
		} else {
			c = new Conversation();
			c.setAlias(conversation.getRoomName());
			c.setType(conversation.getChatType().name());
			FAKE_IDS++;
			c.setId(FAKE_IDS);
		}
		c.setPhone(conversation.getPhoneNumber());
		Model model = new Model(Application.getContext());
		model.parse(c, System.currentTimeMillis(), null);
		model.save();
		return c;
	}

}
