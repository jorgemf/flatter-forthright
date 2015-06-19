package com.livae.ff.app.task;

import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskForthrightUnblock extends NetworkAsyncTask<Void, Void> {

	@Override
	protected Void doInBackground(Void aVoid) throws Exception {
		API.endpoint().unblockForthright().execute();
		Application.appUser().setBlockedForthRightChats(null);
		return null;
	}

}