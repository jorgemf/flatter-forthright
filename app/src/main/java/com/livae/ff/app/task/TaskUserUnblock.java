package com.livae.ff.app.task;

import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;

public class TaskUserUnblock extends NetworkAsyncTask<Long, Void> {

	@Override
	protected Void doInBackground(Long phone)
	  throws Exception {
		API.endpoint().unblockUser(phone).execute();
		Model model = new Model(Application.getContext());
		model.parseBlocked(phone, false);
		model.save();
		return null;
	}

}
