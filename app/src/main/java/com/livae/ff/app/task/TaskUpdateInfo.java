package com.livae.ff.app.task;

import com.livae.apphunt.api.apphunt.model.HuntUser;
import com.livae.apphunt.api.apphunt.model.UserData;
import com.livae.apphunt.app.AppUser;
import com.livae.apphunt.app.Application;
import com.livae.apphunt.app.api.API;
import com.livae.apphunt.app.api.Model;
import com.livae.apphunt.app.async.NetworkAsyncTask;

public class TaskUpdateInfo extends NetworkAsyncTask<UserData, HuntUser> {

	@Override
	protected HuntUser doInBackground(UserData userData) throws Exception {
		HuntUser huntUser = API.user().updateUserInfo(userData).execute();
		AppUser user = Application.appUser();
		user.setName(huntUser.getName());
		user.setTagline(huntUser.getTagLine());
		user.setCountry(huntUser.getCountry());
		Model model = Application.model();
		model.parse(huntUser);
		model.save();
		return huntUser;
	}
}