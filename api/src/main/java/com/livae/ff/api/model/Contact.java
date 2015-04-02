package com.livae.ff.api.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.io.Serializable;

@Entity
@Cache
public class Contact implements Serializable {

	@Id
	private Long id;

	private Long userPhone;

	private Long contactPhone;

	public Contact() {

	}

	public Contact(Long userPhone, Long contactPhone) {
		this.userPhone = userPhone;
		this.contactPhone = contactPhone;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(Long userPhone) {
		this.userPhone = userPhone;
	}

	public Long getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(Long contactPhone) {
		this.contactPhone = contactPhone;
	}
}
