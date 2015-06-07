package com.livae.ff.app.async;

import android.os.AsyncTask;
import android.util.Log;

import com.livae.ff.app.Analytics;

public abstract class CustomAsyncTask<Param, Result> {

	public static final String LOG_TAG = "TASK";

	private AsyncTask<Parameter, Void, Results> asyncTask;

	private boolean cancelled;

	public CustomAsyncTask<Param, Result> execute(Param param, Callback<Param, Result> callback) {
		cancelled = false;
		asyncTask = new AsyncTask<Parameter, Void, Results>() {

			@Override
			@SafeVarargs
			protected final Results doInBackground(Parameter... params) {
				Param parameters = params[0].param;
				Callback<Param, Result> callback = params[0].callback;
				try {
					Log.i(LOG_TAG,
						  CustomAsyncTask.this.getClass().getSimpleName() + " " + parameters);
					Result result = CustomAsyncTask.this.doInBackground(parameters);
					return new Results(parameters, result, callback);
				} catch (Exception e) {
					return new Results(parameters, e, callback);
				}
			}

			@Override
			protected void onPostExecute(Results results) {
				executeCallback(results);
			}
		};

		//noinspection unchecked
		asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Parameter(param, callback));

		return this;
	}

	protected void executeCallback(Results result) {
		asyncTask = null;
		if (result.callback != null) {
			try {
				if (result.error != null) {
					Log.e(LOG_TAG, "Error executing the task: " + this.getClass().getSimpleName(),
						  result.error);
					result.callback.onError(this, result.param, result.error);
				} else {
					Log.i(LOG_TAG,
						  CustomAsyncTask.this.getClass().getSimpleName() + " COMPLETED: " +
						  result.result);
					result.callback.onComplete(this, result.param, result.result);
				}
			} catch (Exception e) {
				Analytics.logAndReport(e);
			}
		}
	}

	protected abstract Result doInBackground(Param param) throws Exception;

	public void cancel() {
		cancelled = true;
		if (asyncTask != null) {
			asyncTask.cancel(true);
		}
	}

	public boolean isCancelled() {
		return cancelled;
	}

	class Parameter {

		public Param param;

		public Callback<Param, Result> callback;

		Parameter(Param param, Callback<Param, Result> callback) {
			this.param = param;
			this.callback = callback;
		}
	}

	class Results {

		public Param param;

		public Result result;

		public Exception error;

		public Callback<Param, Result> callback;

		Results(Param param, Result result, Exception error, Callback<Param, Result> callback) {
			this.param = param;
			this.result = result;
			this.error = error;
			this.callback = callback;
		}

		Results(Param param, Result result, Callback<Param, Result> callback) {
			this.param = param;
			this.result = result;
			this.error = null;
			this.callback = callback;
		}

		Results(Param param, Exception error, Callback<Param, Result> callback) {
			this.param = param;
			this.result = null;
			this.error = error;
			this.callback = callback;
		}
	}

}
