package com.livae.ff.api.model;

import com.googlecode.objectify.stringifier.Stringifier;

public class LongStringifier implements Stringifier<Long> {

	@Override
	public String toString(Long obj) {
		return Long.toString(obj, 32);
	}

	@Override
	public Long fromString(String str) {
		return Long.parseLong(str, 32);
	}
}