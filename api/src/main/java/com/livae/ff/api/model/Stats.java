package com.livae.ff.api.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

@Entity
public class Stats {

	@Id
	private Long id;

	@Index
	private Date date;

	private Long totalUsers;

	private Long totalComments;

	private Long totalConversations;

	private Long totalCommentVotes;

	private Long totalFlags;

	private Long activeUsersLast1Day;

	private Long activeUsersLast7Days;

	private Long activeUsersLast15Days;

	private Long activeUsersLast30Days;

	private Long createdUsersLast1Day;

	private Long createdUsersLast7Days;

	private Long createdUsersLast15Days;

	private Long createdUsersLast30Days;

	private Long privateMessages;

	private Long secretMessages;

	private Long anonymousMessages;

	private Long flatteredMessages;

	private Long forthrightMessages;

	public Stats() {
		date = new Date();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Long getTotalUsers() {
		return totalUsers;
	}

	public void setTotalUsers(Long totalUsers) {
		this.totalUsers = totalUsers;
	}

	public Long getTotalComments() {
		return totalComments;
	}

	public void setTotalComments(Long totalComments) {
		this.totalComments = totalComments;
	}

	public Long getTotalConversations() {
		return totalConversations;
	}

	public void setTotalConversations(Long totalConversations) {
		this.totalConversations = totalConversations;
	}

	public Long getTotalCommentVotes() {
		return totalCommentVotes;
	}

	public void setTotalCommentVotes(Long totalCommentVotes) {
		this.totalCommentVotes = totalCommentVotes;
	}

	public Long getTotalFlags() {
		return totalFlags;
	}

	public void setTotalFlags(Long totalFlags) {
		this.totalFlags = totalFlags;
	}

	public Long getActiveUsersLast1Day() {
		return activeUsersLast1Day;
	}

	public void setActiveUsersLast1Day(Long activeUsersLast1Day) {
		this.activeUsersLast1Day = activeUsersLast1Day;
	}

	public Long getActiveUsersLast7Days() {
		return activeUsersLast7Days;
	}

	public void setActiveUsersLast7Days(Long activeUsersLast7Days) {
		this.activeUsersLast7Days = activeUsersLast7Days;
	}

	public Long getActiveUsersLast15Days() {
		return activeUsersLast15Days;
	}

	public void setActiveUsersLast15Days(Long activeUsersLast15Days) {
		this.activeUsersLast15Days = activeUsersLast15Days;
	}

	public Long getActiveUsersLast30Days() {
		return activeUsersLast30Days;
	}

	public void setActiveUsersLast30Days(Long activeUsersLast30Days) {
		this.activeUsersLast30Days = activeUsersLast30Days;
	}

	public Long getCreatedUsersLast1Day() {
		return createdUsersLast1Day;
	}

	public void setCreatedUsersLast1Day(Long createdUsersLast1Day) {
		this.createdUsersLast1Day = createdUsersLast1Day;
	}

	public Long getCreatedUsersLast7Days() {
		return createdUsersLast7Days;
	}

	public void setCreatedUsersLast7Days(Long createdUsersLast7Days) {
		this.createdUsersLast7Days = createdUsersLast7Days;
	}

	public Long getCreatedUsersLast15Days() {
		return createdUsersLast15Days;
	}

	public void setCreatedUsersLast15Days(Long createdUsersLast15Days) {
		this.createdUsersLast15Days = createdUsersLast15Days;
	}

	public Long getCreatedUsersLast30Days() {
		return createdUsersLast30Days;
	}

	public void setCreatedUsersLast30Days(Long createdUsersLast30Days) {
		this.createdUsersLast30Days = createdUsersLast30Days;
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
}
