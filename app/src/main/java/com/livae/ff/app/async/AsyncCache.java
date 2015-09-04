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

	public static synchronized AsyncCache instance() {
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

	protected synchronized void taskStarted(CustomAsyncTask task) {
		getList(task.getLifeCycle()).add(task);
	}

	protected synchronized void taskFinished(CustomAsyncTask task) {
		getList(task.getLifeCycle()).remove(task);
	}

	public synchronized CustomAsyncTask getTask(LifeCycle lifeCycle, Class<CustomAsyncTask> c) {
		List<CustomAsyncTask> list = new ArrayList<>();
		list.addAll(getList(lifeCycle)); // this avoids concurrent modifications with iterator
		for (CustomAsyncTask task : list) {
			if (task.getClass().getCanonicalName().equals(c.getCanonicalName())) {
				return task;
			}
		}
		return null;
	}

	@Override
	public synchronized void onResume(LifeCycle lifeCycle) {
		// set all lifecycle in tasks and execute the pending ones
		List<CustomAsyncTask> list = new ArrayList<>();
		list.addAll(getList(lifeCycle)); // this avoids concurrent modifications with iterator
		for (CustomAsyncTask task : list) {
			//noinspection unchecked
			task.setLifeCycle(lifeCycle);
			if (task.getStatus() == CustomAsyncTask.STATUS.WAITING) {
				//noinspection unchecked
				task.executeWaitingCallback(lifeCycle);
			}
		}
	}

	@Override
	public synchronized void onPause(LifeCycle lifeCycle) {
		// set lifecycle in tasks as null
		List<CustomAsyncTask> list = new ArrayList<>();
		list.addAll(getList(lifeCycle)); // this avoids concurrent modifications with iterator
		for (CustomAsyncTask task : list) {
			//noinspection unchecked
			task.setLifeCycle(null);
		}
	}

	@Override
	public synchronized void onDestroy(LifeCycle lifeCycle) {
		// nothing
	}

	@Override
	public synchronized void onCreate(LifeCycle lifeCycle) {
		// empty list of lifecycle as the object was created for first time
		final String name = lifeCycle.getClass().getName();
		cache.remove(name);
	}

	@Override
	public synchronized void onRecreate(LifeCycle lifeCycle) {
		// nothing
	}
}
