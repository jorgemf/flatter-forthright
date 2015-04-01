package com.livae.ff.api.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.io.Serializable;

import javax.annotation.Nonnull;

import static com.livae.ff.api.OfyService.ofy;

@Entity
@Cache
@SuppressWarnings("UnusedDeclaration")
public class AdminUser implements Serializable {

	@Id
	private Long phone;

	public AdminUser() {
	}

	public AdminUser(@Nonnull Long phone) {
		this.phone = phone;
	}

	public static AdminUser get(Long phone) {
		return ofy().load().type(AdminUser.class).id(phone).now();
	}

	public Long getPhone() {
		return phone;
	}

	public void setPhone(Long phone) {
		this.phone = phone;
	}
}
