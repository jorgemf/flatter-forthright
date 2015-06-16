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

	private Profile profile;

	private Date forthrightChatsDateBlocked;

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Numbers blockedChats;

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private NumbersDate blockedAnonymousChats;

	// TODO some statistics
	// times flagged
	// times flagged each type
	// times agreed with comment
	// times disagreed with comments

	public PhoneUser() {

	}

	public PhoneUser(@Nonnull Long phone) {
		this.phone = phone;
		forthrightChatsDateBlocked = null;
		lastAccess = new Date();
		created = new Date();
		blockedChats = new Numbers();
		blockedAnonymousChats = new NumbersDate();
	}

	public static PhoneUser get(Long phone) {
		return ofy().load().type(PhoneUser.class).id(phone).now();
	}

	public void addBlockedPhone(Long phone) {
		blockedChats.addNumber(phone);
	}

	public void removeBlockedPhone(Long phone) {
		blockedChats.removeNumber(phone);
	}

	public void addBlockedAnonymousPhone(Long phone, Date date) {
		blockedAnonymousChats.addNumber(phone, date);
	}

	public boolean isBlockedPhone(Long phone) {
		return blockedChats.containsNumber(phone);
	}

	public boolean isBlockedAnonymousPhone(Long phone) {
		Date date = blockedAnonymousChats.containsNumber(phone);
		if (date != null) {
			if (date.getTime() < System.currentTimeMillis()) {
				blockedAnonymousChats.removeNumber(phone);
				date = null;
				ofy().save().entity(this);
			}
		}
		return date != null;
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

	public Date getForthrightChatsDateBlocked() {
		return forthrightChatsDateBlocked;
	}

	public void setForthrightChatsDateBlocked(Date forthrightChatsDateBlocked) {
		this.forthrightChatsDateBlocked = forthrightChatsDateBlocked;
	}
}
