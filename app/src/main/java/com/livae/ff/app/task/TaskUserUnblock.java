package com.livae.ff.app.task;

import android.support.annotation.NonNull;

import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.LifeCycle;

public class TaskUserUnblock extends NetworkAsyncTask<LifeCycle, Long, Void> {

	public TaskUserUnblock(@NonNull LifeCycle lifeCycle) {
		super(lifeCycle);
	}

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
