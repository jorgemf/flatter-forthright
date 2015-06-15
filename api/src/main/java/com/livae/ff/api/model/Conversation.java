package com.livae.ff.api.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfNotNull;
import com.livae.ff.common.Constants.ChatType;

import java.util.ArrayList;
import java.util.Collection;

public class Conversation {

	@Id
	private Long id;

	private ChatType type;

	private String alias;

	@Index(IfNotNull.class)
	private Long phone;

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Collection<Long> users;

	public Conversation() {
	}

	public Conversation(ChatType type) {
		this.type = type;
		users = new ArrayList<>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ChatType getType() {
		return type;
	}

	public void setType(ChatType type) {
		this.type = type;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Long getPhone() {
		return phone;
	}

	public void setPhone(Long phone) {
		this.phone = phone;
	}

	public Collection<Long> getUsers() {
		return users;
	}

	public void addUser(Long phone) {
		if (!users.contains(phone)) {
			users.add(phone);
		}
	}

	public void removeUser(Long phone) {
		users.remove(phone);
	}
}
