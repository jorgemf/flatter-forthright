package com.livae.ff.app.task;

import com.livae.apphunt.app.api.API;
import com.livae.apphunt.app.async.NetworkAsyncTask;

public class TaskDeleteComment extends NetworkAsyncTask<Long, Void> {

	@Override
	protected Void doInBackground(Long id) throws Exception {
		API.admin().deleteComment(id).execute();
		return null;
	}
}