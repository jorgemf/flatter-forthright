package com.livae.ff.app.service;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.livae.ff.app.Application;

public class IDService extends InstanceIDListenerService {

	@Override
	public void onTokenRefresh() {
		Application.appUser().setCloudMessagesId(null); // so it will set in the wakeup call later
	}
}
