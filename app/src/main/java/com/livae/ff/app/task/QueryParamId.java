package com.livae.ff.app.task;

import com.livae.apphunt.common.Constants.Order;

public class QueryParamId extends QueryParam {

	private Long id;

	public QueryParamId(Long id, Integer limit, Order order) {
		super(limit, order);
		this.id = id;
	}

	public QueryParamId(Long id, Order order) {
		this(id, null, order);
	}

	public QueryParamId(Long id, Integer limit) {
		this(id, limit, null);
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
