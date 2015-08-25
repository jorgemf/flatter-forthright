package com.livae.ff.app.task;

import android.support.annotation.NonNull;

import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.LifeCycle;

public class TaskForthrightBlock extends NetworkAsyncTask<LifeCycle, Void, Void> {

	public TaskForthrightBlock(@NonNull LifeCycle lifeCycle) {
		super(lifeCycle);
	}

	@Override
	protected Void doInBackground(Void aVoid)
	  throws Exception {
		API.endpoint().blockForthright().execute();
		Application.appUser().setBlockedForthRightChats(System.currentTimeMillis());
		return null;
	}

}
