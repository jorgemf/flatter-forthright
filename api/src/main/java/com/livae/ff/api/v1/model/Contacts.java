package com.livae.ff.api.v1.model;

import java.util.Collection;

public class Contacts {

	private Collection<String> numbers;

	private String countryCode;

	public Contacts() {

	}

	public Collection<String> getNumbers() {
		return numbers;
	}

	public void setNumbers(Collection<String> numbers) {
		this.numbers = numbers;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
}
