package com.livae.ff.api.model;

import java.util.Date;

public class UserDate {

	private Long phone;

	private Date date;

	public UserDate() {
	}

	public UserDate(Long phone, Date date) {
		this.phone = phone;
		this.date = date;
	}

	public Long getPhone() {
		return phone;
	}

	public void setPhone(Long phone) {
		this.phone = phone;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
