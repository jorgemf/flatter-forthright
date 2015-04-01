package com.livae.ff.app.task;

import com.livae.apphunt.common.Constants.Order;

public class QueryParam {

	private String cursor;

	private Integer limit;

	private Order order;

	public QueryParam(Integer limit, Order order) {
		this.limit = limit;
		this.order = order;
	}

	public QueryParam(Order order) {
		this.order = order;
	}

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

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	@Override
	public String toString() {
		return "[limit: " + limit + "] [order: " + order.name() + "] [cursor: " + cursor + "]";
	}
}
