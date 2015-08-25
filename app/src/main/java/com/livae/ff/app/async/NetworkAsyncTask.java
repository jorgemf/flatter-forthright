package com.livae.ff.app.async;

import android.support.annotation.NonNull;

import com.livae.ff.app.Application;
import com.livae.ff.app.listener.LifeCycle;
import com.livae.ff.app.utils.DeviceUtils;

public abstract class NetworkAsyncTask<A extends LifeCycle, Param, Result>
  extends CustomAsyncTask<A, Param, Result> {

	public NetworkAsyncTask(@NonNull A a) {
		super(a);
	}

	public final CustomAsyncTask<A, Param, Result> execute(Param param,
														   Callback<A, Param, Result> callback) {
		if (DeviceUtils.isNetworkAvailable(Application.getContext())) {
			super.execute(param, callback);
		} else if (callback != null) {
			status = STATUS.FINISHED;
			AsyncCache.instance().taskFinished(this);
			callback.onError(getLifeCycle(), param, new NoNetworkException());
		}
		return this;
	}
}
