package com.livae.ff.app.async;

import com.livae.ff.app.Application;
import com.livae.ff.app.activity.AbstractActivity;
import com.livae.ff.app.fragment.AbstractFragment;
import com.livae.ff.app.utils.DeviceUtils;

public abstract class NetworkAsyncTask<Param, Result> extends CustomAsyncTask<Param, Result> {

	public static final String LOG_TAG = "TASK_NETWORK";

	public NetworkAsyncTask(AbstractActivity abstractActivity) {
		super(abstractActivity);
	}

	public NetworkAsyncTask(AbstractFragment abstractFragment) {
		super(abstractFragment);
	}

	public NetworkAsyncTask() {
	}

	public final CustomAsyncTask<Param, Result> execute(Param param,
														Callback<Param, Result> callback) {
		if (DeviceUtils.isNetworkAvailable(Application.getContext())) {
			super.execute(param, callback);
		} else if (callback != null) {
			callback.onError(this, param, new NoNetworkException());
		}
		return this;
	}
}
