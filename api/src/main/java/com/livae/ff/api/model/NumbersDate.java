package com.livae.ff.api.model;

import com.googlecode.objectify.annotation.Entity;

import java.util.Date;
import java.util.HashMap;

@Entity
public class NumbersDate {

	private HashMap<Long, Date> numbers;

	public NumbersDate() {
		numbers = new HashMap<>();
	}

	public void addNumber(Long number, Date date) {
		numbers.put(number, date);
	}

	public void removeNumber(Long number) {
		numbers.remove(number);
	}

	public Date containsNumber(Long number) {
		return numbers.get(number);
	}
}
