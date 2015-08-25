package com.livae.ff.app.task;

import android.support.annotation.NonNull;

import com.livae.ff.app.Application;
import com.livae.ff.app.api.API;
import com.livae.ff.app.api.Model;
import com.livae.ff.app.async.NetworkAsyncTask;
import com.livae.ff.app.listener.LifeCycle;

public class TaskUserBlock extends NetworkAsyncTask<LifeCycle, Long, Void> {

	public TaskUserBlock(@NonNull LifeCycle lifeCycle) {
		super(lifeCycle);
	}

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
