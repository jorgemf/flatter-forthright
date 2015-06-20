package com.livae.ff.app.task;

public class QueryId extends QueryParam {

	private Long id;

	public QueryId(Long id) {
		super();
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "[id: " + id + "] " + super.toString();
	}
}
