package com.livae.ff.app.async;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.livae.ff.app.listener.LifeCycle;

/**
 * Custom AsyncTask to deal with all the issues of async task in activiy and fragments life cycle
 *
 * @param <A>s
 * @param <Param>
 * @param <Result>
 */
public abstract class CustomAsyncTask<A extends LifeCycle, Param, Result> {

	public static final String LOG_TAG = "TASK";

	protected STATUS status;

	private AsyncTask<Parameter, Void, Results> asyncTask;

	private boolean cancelled;

	private A a;

	private Results results;

	public CustomAsyncTask(@NonNull A a) {
		this.a = a;
		status = STATUS.CREATED;
	}

	public A getLifeCycle() {
		return a;
	}

	public void setLifeCycle(A a) {
		this.a = a;
	}

	public STATUS getStatus() {
		return status;
	}

	public CustomAsyncTask<A, Param, Result> execute(Param param,
													 Callback<A, Param, Result> callback) {
		if (status != STATUS.CREATED && status != STATUS.FINISHED) {
			throw new RuntimeException("Task was already running");
		}
		cancelled = false;
		AsyncCache.instance().taskStarted(this);
		asyncTask = new AsyncTask<Parameter, Void, Results>() {

			@Override
			@SafeVarargs
			protected final Results doInBackground(Parameter... params) {
				status = STATUS.STARTED;
				Param parameters = params[0].param;
				Callback<A, Param, Result> callback = params[0].callback;
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
				if (!isCancelled()) {
					if (a == null) {
						status = STATUS.WAITING;
						CustomAsyncTask.this.results = results;
					} else {
						executeCallback(results);
					}
				} else {
					status = STATUS.FINISHED;
					cancelled = false;
					AsyncCache.instance().taskFinished(CustomAsyncTask.this);
				}
			}
		};

		final Parameter parameters = new Parameter(param, callback);
		//noinspection unchecked
		asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, parameters);

		return this;
	}

	protected void executeWaitingCallback(A a) {
		if (status == STATUS.WAITING && a != null && results != null) {
			this.a = a;
			executeCallback(results);
		}
	}

	protected void executeCallback(Results result) {
		status = STATUS.FINISHED;
		cancelled = false;
		AsyncCache.instance().taskFinished(this);
		asyncTask = null;
		if (result.callback != null) {
			if (result.error != null) {
				Log.e(LOG_TAG, "Error executing the task: " + this.getClass().getSimpleName(),
					  result.error);
				result.callback.onError(a, result.param, result.error);
			} else {
				Log.i(LOG_TAG, CustomAsyncTask.this.getClass().getSimpleName() +
							   " COMPLETED: " + result.result);
				result.callback.onComplete(a, result.param, result.result);
			}
		}
	}

	protected abstract Result doInBackground(Param param)
	  throws Exception;

	public void cancel() {
		if (status == STATUS.STARTED || status == STATUS.WAITING) {
			cancelled = true;
			if (asyncTask != null) {
				asyncTask.cancel(true);
			}
		}
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public enum STATUS {CREATED, STARTED, WAITING, FINISHED}

	class Parameter {

		public Param param;

		public Callback<A, Param, Result> callback;

		Parameter(Param param, Callback<A, Param, Result> callback) {
			this.param = param;
			this.callback = callback;
		}
	}

	class Results {

		public Param param;

		public Result result;

		public Exception error;

		public Callback<A, Param, Result> callback;

		Results(Param param, Result result, Exception error, Callback<A, Param, Result> callback) {
			this.param = param;
			this.result = result;
			this.error = error;
			this.callback = callback;
		}

		Results(Param param, Result result, Callback<A, Param, Result> callback) {
			this.param = param;
			this.result = result;
			this.error = null;
			this.callback = callback;
		}

		Results(Param param, Exception error, Callback<A, Param, Result> callback) {
			this.param = param;
			this.result = null;
			this.error = error;
			this.callback = callback;
		}
	}

}
