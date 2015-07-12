package com.livae.ff.api.model;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
@Cache
public class CounterStats {

	@Id
	private Long id;

	private Long privateMessages;

	private Long secretMessages;

	private Long anonymousMessages;

	private Long flatteredMessages;

	private Long forthrightMessages;

	public CounterStats() {
		privateMessages = 0L;
		secretMessages = 0L;
		anonymousMessages = 0L;
		flatteredMessages = 0L;
		forthrightMessages = 0L;
	}

	public Long getPrivateMessages() {
		return privateMessages;
	}

	public void setPrivateMessages(Long privateMessages) {
		this.privateMessages = privateMessages;
	}

	public Long getSecretMessages() {
		return secretMessages;
	}

	public void setSecretMessages(Long secretMessages) {
		this.secretMessages = secretMessages;
	}

	public Long getAnonymousMessages() {
		return anonymousMessages;
	}

	public void setAnonymousMessages(Long anonymousMessages) {
		this.anonymousMessages = anonymousMessages;
	}

	public Long getFlatteredMessages() {
		return flatteredMessages;
	}

	public void setFlatteredMessages(Long flatteredMessages) {
		this.flatteredMessages = flatteredMessages;
	}

	public Long getForthrightMessages() {
		return forthrightMessages;
	}

	public void setForthrightMessages(Long forthrightMessages) {
		this.forthrightMessages = forthrightMessages;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getTotal() {
		return privateMessages + secretMessages + secretMessages + flatteredMessages +
			   forthrightMessages;
	}
}
