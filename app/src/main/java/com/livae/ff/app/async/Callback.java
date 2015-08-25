package com.livae.ff.app.async;

import android.support.annotation.NonNull;

import com.livae.ff.app.listener.LifeCycle;

public interface Callback<A extends LifeCycle, Param, Result> {

	void onComplete(@NonNull A a, Param param, Result result);

	void onError(@NonNull A a, Param param, @NonNull Exception e);
}
