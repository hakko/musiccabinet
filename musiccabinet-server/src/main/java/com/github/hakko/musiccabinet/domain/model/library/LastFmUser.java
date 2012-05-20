package com.github.hakko.musiccabinet.domain.model.library;

public class LastFmUser {

	private String lastFmUser;

	public LastFmUser(String lastFmUser) {
		this.lastFmUser = lastFmUser;
	}
	
	public String getLastFmUser() {
		return lastFmUser;
	}

	public void setLastFmUser(String lastFmUser) {
		this.lastFmUser = lastFmUser;
	}

	public String toString() {
		return "User " + lastFmUser;
	}
	
}