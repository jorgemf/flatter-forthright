package com.livae.ff.api.model;

import com.googlecode.objectify.annotation.Entity;

import java.util.Collection;
import java.util.HashSet;

@Entity
public class Numbers {

	private Collection<Long> numbers;

	public Numbers() {
		numbers = new HashSet<>();
	}

	public void addNumber(Long number) {
		if(!numbers.contains(number)) {
			numbers.add(number);
		}
	}

	public void removeNumber(Long number) {
		numbers.remove(number);
	}

	public boolean containsNumber(Long number){
		return numbers.contains(number);
	}

	public Collection<Long> getCollection() {
		return numbers;
	}
}
