package com.livae.ff.app.async;

public class NoNetworkException extends Exception {

	public NoNetworkException() {
		super("There is no network available");
	}

}
