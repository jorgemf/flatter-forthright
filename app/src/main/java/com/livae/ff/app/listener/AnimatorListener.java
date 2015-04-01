package com.livae.ff.app.listener;

import android.animation.AnimatorListenerAdapter;

public class AnimatorListener extends AnimatorListenerAdapter {

	private boolean enable;

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	protected boolean isEnabled() {
		return this.enable;
	}

}
