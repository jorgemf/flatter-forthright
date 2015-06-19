package com.livae.ff.app.task;

import com.livae.ff.api.ff.model.Numbers;
import com.livae.ff.api.ff.model.PhoneUser;
import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskUserInfoGet extends NetworkAsyncTask<Void, Void> {

	@Override
	protected Void doInBackground(Void aVoid) throws Exception {
		// update blocked anonymous chats
		PhoneUser user = API.endpoint().getUserInfo().execute();
		Application.appUser().setBlockedForthRightChats(user.getForthrightChatsDateBlocked()
															.getValue());
		// update blocked numbers
		Numbers blockedNumbers = API.endpoint().getBlockedUsers().execute();
		Model model = new Model(Application.getContext());
		model.parseBlocked(blockedNumbers);
		model.save();
		return null;
	}

}