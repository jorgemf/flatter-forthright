package com.livae.ff.app.task;

public class ListResult {

	private String nextCursor;

	private int size;

	public ListResult(String nextCursor, int size) {
		this.nextCursor = nextCursor;
		this.size = size;
	}

	public String getNextCursor() {
		return nextCursor;
	}

	public int getSize() {
		return size;
	}

	@Override
	public String toString() {
		return "[Size: " + size + "] [next: " + nextCursor + "]";
	}
}
