package com.livae.ff.app.task;

import android.util.Log;

import com.livae.ff.app.api.API;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskWakeup extends NetworkAsyncTask<Void, Void> {

	@Override
	protected Void doInBackground(Void aVoid) throws Exception {
		API.wakeup();
		Log.d(LOG_TAG, "wakeup sent");
		return null;
	}

}