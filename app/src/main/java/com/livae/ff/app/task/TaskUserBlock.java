package com.livae.ff.app.task;

import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskUserBlock extends NetworkAsyncTask<Long, Void> {

	@Override
	protected Void doInBackground(Long phone)
	  throws Exception {
		API.endpoint().blockUser(phone).execute();
		Model model = new Model(Application.getContext());
		model.parseBlocked(phone, true);
		model.save();
		return null;
	}

}
