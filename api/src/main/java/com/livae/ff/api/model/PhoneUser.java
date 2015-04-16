package com.livae.ff.api.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.livae.ff.common.Constants.Profile;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nonnull;

import static com.livae.ff.api.OfyService.ofy;

@Entity
@Cache
public class PhoneUser implements Serializable {

	@Id
	private Long phone;

	@Index
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Date created;

	@Index
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Date lastAccess;

	@Index
	private String authToken;

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Profile profile;

	public PhoneUser() {

	}

	public PhoneUser(@Nonnull Long phone) {
		this.phone = phone;
		lastAccess = new Date();
		created = new Date();
	}

	public static PhoneUser get(Long phone) {
		return ofy().load().type(PhoneUser.class).id(phone).now();
	}

	public Long getPhone() {
		return phone;
	}

	public void setPhone(Long phone) {
		this.phone = phone;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastAccess() {
		return lastAccess;
	}

	public void setLastAccess(Date lastAccess) {
		this.lastAccess = lastAccess;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}
}