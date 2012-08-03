package com.github.hakko.musiccabinet.domain.model.library;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class LastFmUser {

	private int id = -1;
	private String lastFmUsername;
	private String sessionKey;

	public LastFmUser() {
	}
	
	public LastFmUser(String lastFmUser) {
		this.lastFmUsername = lastFmUser;
	}

	public LastFmUser(String lastFmUser, String sessionKey) {
		this.lastFmUsername = lastFmUser;
		this.sessionKey = sessionKey;
	}
	
	public LastFmUser(int id, String lastFmUsername, String sessionKey) {
		this.id = id;
		this.lastFmUsername = lastFmUsername;
		this.sessionKey = sessionKey;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String getLastFmUsername() {
		return lastFmUsername;
	}

	public void setLastFmUsername(String lastFmUsername) {
		this.lastFmUsername = lastFmUsername;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
		.append(lastFmUsername)
		.append(sessionKey)
		.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (o.getClass() != getClass()) return false;

		LastFmUser u = (LastFmUser) o;
		return new EqualsBuilder()
		.append(lastFmUsername, u.lastFmUsername)
		.append(sessionKey, u.sessionKey)
		.isEquals();
	}

	@Override
	public String toString() {
		return "User " + lastFmUsername;
	}

}