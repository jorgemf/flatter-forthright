package com.livae.ff.app.task;

import android.support.annotation.NonNull;

import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.LifeCycle;

public class TaskForthrightUnblock extends NetworkAsyncTask<LifeCycle, Void, Void> {

	public TaskForthrightUnblock(@NonNull LifeCycle lifeCycle) {
		super(lifeCycle);
	}

	@Override
	protected Void doInBackground(Void aVoid)
	  throws Exception {
		API.endpoint().unblockForthright().execute();
		Application.appUser().setBlockedForthRightChats(null);
		return null;
	}

}
