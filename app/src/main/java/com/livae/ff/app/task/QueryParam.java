package com.livae.ff.app.task;

public class QueryParam {

	private String cursor;

	private Integer limit;

	public QueryParam(Integer limit) {
		this.limit = limit;
	}

	public QueryParam() {

	}

	public String getCursor() {
		return cursor;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	@Override
	public String toString() {
		return "[limit: " + limit + "] [cursor: " + cursor + "]";
	}
}
