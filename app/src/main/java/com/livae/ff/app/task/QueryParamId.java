package com.livae.ff.app.task;

public class QueryParamId extends QueryParam {

	private Long id;

	public QueryParamId(Long id, Integer limit) {
		super(limit);
		this.id = id;
	}

	public QueryParamId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "[id: " + id + "] " + super.toString();
	}
}
