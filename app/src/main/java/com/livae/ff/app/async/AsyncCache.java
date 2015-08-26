package com.livae.ff.app.async;

import com.livae.ff.app.listener.LifeCycle;
import com.livae.ff.app.listener.OnLifeCycleListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This is a cache for the async tasks of the fragments and activities so they can be send the
 * results to the fragment or activity when these are recreated due to a rotation of the device.
 */
public class AsyncCache implements OnLifeCycleListener {

	private static AsyncCache instance;

	private HashMap<String, List<CustomAsyncTask>> cache;

	private AsyncCache() {
		cache = new HashMap<>();
	}

	public static AsyncCache instance() {
		if (instance == null) {
			instance = new AsyncCache();
		}
		return instance;
	}

	private List<CustomAsyncTask> getList(LifeCycle lifeCycle) {
		final String name = lifeCycle.getClass().getName();
		if (!cache.containsKey(name)) {
			cache.put(name, new ArrayList<CustomAsyncTask>());
		}
		return cache.get(name);
	}

	protected void taskStarted(CustomAsyncTask task) {
		getList(task.getLifeCycle()).add(task);
	}

	protected void taskFinished(CustomAsyncTask task) {
		getList(task.getLifeCycle()).remove(task);
	}

	public CustomAsyncTask getTask(LifeCycle lifeCycle, Class<CustomAsyncTask> c) {
		synchronized (this) {
			List<CustomAsyncTask> list = getList(lifeCycle);
			for (CustomAsyncTask task : list) {
				if (task.getClass().getCanonicalName().equals(c.getCanonicalName())) {
					return task;
				}
			}
			return null;
		}
	}

	@Override
	public void onResume(LifeCycle lifeCycle) {
		// set all lifecycle in tasks and execute the pending ones
		synchronized (this) {
			final List<CustomAsyncTask> list = getList(lifeCycle);
			for (CustomAsyncTask task : list) {
				task.setLifeCycle(lifeCycle);
				if (task.getStatus() == CustomAsyncTask.STATUS.WAITING) {
					task.executeWaitingCallback(lifeCycle);
				}
			}
		}
	}

	@Override
	public void onPause(LifeCycle lifeCycle) {
		// set lifecycle in tasks as null
		synchronized (this) {
			final List<CustomAsyncTask> list = getList(lifeCycle);
			for (CustomAsyncTask task : list) {
				task.setLifeCycle(null);
			}
		}
	}

	@Override
	public void onDestroy(LifeCycle lifeCycle) {
		// nothing
	}

	@Override
	public void onCreate(LifeCycle lifeCycle) {
		// empty list of lifecycle as the object was created for first time
		synchronized (this) {
			final String name = lifeCycle.getClass().getName();
			cache.remove(name);
		}
	}

	@Override
	public void onRecreate(LifeCycle lifeCycle) {
		// nothing
	}
}
