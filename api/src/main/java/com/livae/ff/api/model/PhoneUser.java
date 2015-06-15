package com.livae.ff.api.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.livae.ff.common.Constants.Profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

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

	private Boolean privateChats;

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Collection<Long> blockedChats;

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Collection<UserDate> blockedAnonymousChats;

	public PhoneUser() {

	}

	public PhoneUser(@Nonnull Long phone) {
		this.phone = phone;
		privateChats = true;
		lastAccess = new Date();
		created = new Date();
		blockedChats = new ArrayList<>();
		blockedAnonymousChats = new ArrayList<>();
	}

	public static PhoneUser get(Long phone) {
		return ofy().load().type(PhoneUser.class).id(phone).now();
	}

	public void addBlockedPhone(Long phone) {
		if (!blockedChats.contains(phone)) {
			blockedChats.add(phone);
		}
	}

	public void removeBlockedPhone(Long phone) {
		blockedChats.remove(phone);
	}

	public void addBlockedAnonymousPhone(Long phone, Date date) {
		blockedAnonymousChats.add(new UserDate(phone, date));
	}

	public boolean isBlockedPhone(Long phone) {
		for (UserDate userDate : blockedAnonymousChats) {
			if (userDate.getPhone().equals(phone)) {
				return true;
			}
		}
		return false;
	}

	public boolean isBlockedAnonymousPhone(Long phone) {
		pruneBlockedAnonymous();
		for (UserDate userDate : blockedAnonymousChats) {
			if (userDate.getPhone().equals(phone)) {
				return true;
			}
		}
		return false;
	}

	private void pruneBlockedAnonymous() {
		Iterator<UserDate> it = blockedAnonymousChats.iterator();
		long now = System.currentTimeMillis();
		while (it.hasNext()) {
			UserDate next = it.next();
			if (next.getDate().getTime() < now) {
				it.remove();
			}
		}
	}

	public Long getPhone() {
		return phone;
	}

	public void setPhone(Long phone) {
		this.phone = phone;
	}

	public Boolean getPrivateChats() {
		return privateChats;
	}

	public void setPrivateChats(Boolean privateChats) {
		this.privateChats = privateChats;
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
