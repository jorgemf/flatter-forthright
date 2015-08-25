package com.livae.ff.app.listener;

public interface OnLifeCycleListener {

	void onResume(LifeCycle lifeCycle);

	void onPause(LifeCycle lifeCycle);

	void onDestroy(LifeCycle lifeCycle);

	void onCreate(LifeCycle lifeCycle);

	void onRecreate(LifeCycle lifeCycle);
}
