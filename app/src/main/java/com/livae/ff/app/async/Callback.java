package com.livae.ff.app.async;

public interface Callback<Param, Result> {

	public void onComplete(CustomAsyncTask<Param, Result> task, Param param, Result result);

	public void onError(CustomAsyncTask<Param, Result> task, Param param, Exception e);
}