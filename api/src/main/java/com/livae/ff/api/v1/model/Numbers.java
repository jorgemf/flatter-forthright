package com.livae.ff.api.v1.model;

import java.util.ArrayList;
import java.util.Collection;

public class Numbers {

	private Collection<Long> numbers;

	public Numbers() {
		numbers = new ArrayList<>();
	}

	public Collection<Long> getNumbers() {
		return numbers;
	}

	public void setNumbers(Collection<Long> numbers) {
		this.numbers = numbers;
	}

	public void addNumber(Long phone) {
		numbers.add(phone);
	}
}
