package com.livae.ff.app.task;

import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskUserInfoGet extends NetworkAsyncTask<Void, Void> {

	@Override
	protected Void doInBackground(Void aVoid) throws Exception {
		// this class does nothing, new registration means a new user

//		// update blocked anonymous chats
//		PhoneUser user = API.endpoint().getUserInfo().execute();
//		Application.appUser().setBlockedForthRightChats(user.getForthrightChatsDateBlocked()
//															.getValue());
//		// update blocked numbers
//		Numbers blockedNumbers = API.endpoint().getBlockedUsers().execute();
//		Model model = new Model(Application.getContext());
//		model.parseBlocked(blockedNumbers);
//		model.save();
		return null;
	}

}