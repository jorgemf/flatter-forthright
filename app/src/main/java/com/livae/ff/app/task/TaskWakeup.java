package com.livae.ff.app.task;

import android.util.Log;

import com.livae.apphunt.api.apphunt.model.HuntUser;
import com.livae.apphunt.app.AppUser;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.api.API;
import com.livae.apphunt.app.async.NetworkAsyncTask;

public class TaskWakeup extends NetworkAsyncTask<Void, Void> {

	@Override
	protected Void doInBackground(Void aVoid) throws Exception {
		API.wakeup();
		Log.d(LOG_TAG, "wakeup sent");
		HuntUser user = API.user().getUserInfo().execute();
		AppUser appUser = Application.appUser();
		appUser.setUserId(user.getId());
		appUser.setName(user.getName());
		appUser.setTagline(user.getTagLine());
		appUser.setCountry(user.getCountry());
		return null;
	}

}