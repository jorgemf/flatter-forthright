package com.livae.ff.api.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Stringify;
import com.googlecode.objectify.condition.IfNotNull;
import com.livae.ff.common.Constants.ChatType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.livae.ff.api.OfyService.ofy;

@Entity
@Cache
public class Conversation {

	@Id
	private Long id;

	@Index
	private ChatType type;

	private String alias;

	@Index(IfNotNull.class)
	private Long phone;

	@Index(IfNotNull.class)
	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private String phones;

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	private Collection<Long> users;

	@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
	@Stringify(com.livae.ff.api.model.LongStringifier.class)
	private Map<Long, Date> usersNotification;

	public Conversation() {
	}

	public Conversation(ChatType type) {
		this.type = type;
		users = new ArrayList<>();
		usersNotification = new HashMap<>();
	}

	public static Conversation get(Long id) {
		return ofy().load().type(Conversation.class).id(id).now();
	}

	public static String mixPhones(Long p1, Long p2) {
		char separator = ' ';
		if (p1 > p2) {
			return p1.toString() + separator + p2.toString();
		} else {
			return p2.toString() + separator + p1.toString();
		}
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

	public String getPhones() {
		return phones;
	}

	public void setPhones(String phones) {
		this.phones = phones;
	}

	public void setPhones(Long p1, Long p2) {
		setPhones(mixPhones(p1, p2));
	}

	public void addUserNotification(Long user, Date timeout) {
		if (timeout.getTime() > System.currentTimeMillis()) {
			usersNotification.put(user, timeout);
		}
	}

	public void removeUserNotification(Long user) {
		usersNotification.remove(user);
	}

	public Collection<Long> getUsersNotification() {
		List<Long> numbers = new ArrayList<>(usersNotification.size());
		List<Long> numbersToRemove = new ArrayList<>();
		long now = System.currentTimeMillis();
		for (Map.Entry<Long, Date> entry : usersNotification.entrySet()) {
			if (entry.getValue().getTime() > now) {
				numbers.add(entry.getKey());
			} else {
				numbersToRemove.add(entry.getKey());
			}
		}
		for (Long number : numbersToRemove) {
			usersNotification.remove(number);
		}
		return numbers;
	}
}
