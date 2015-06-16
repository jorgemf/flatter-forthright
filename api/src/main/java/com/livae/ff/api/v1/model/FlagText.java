package com.livae.ff.api.v1.model;

import com.livae.ff.common.Constants.FlagReason;

public class FlagText extends Text {

	private FlagReason reason;

	public FlagText() {
	}

	public FlagText(String text, FlagReason reason) {
		super(text);
		this.reason = reason;
	}

	public FlagReason getReason() {
		return reason;
	}

	public void setReason(FlagReason reason) {
		this.reason = reason;
	}
}