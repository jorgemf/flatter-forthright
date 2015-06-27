package com.livae.ff.app.task;

import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskForthrightBlock extends NetworkAsyncTask<Void, Void> {

	@Override
	protected Void doInBackground(Void aVoid) throws Exception {
		API.endpoint().blockForthright().execute();
		Application.appUser().setBlockedForthRightChats(System.currentTimeMillis());
		return null;
	}

}