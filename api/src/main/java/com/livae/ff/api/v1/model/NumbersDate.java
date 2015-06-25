package com.livae.ff.api.v1.model;

import java.util.Date;
import java.util.HashMap;

public class NumbersDate {

	private HashMap<Long, Date> numbers;

	public NumbersDate() {
	}

	public HashMap<Long, Date> getNumbers() {
		return numbers;
	}

	public void setNumbers(HashMap<Long, Date> numbers) {
		this.numbers = numbers;
	}
}
