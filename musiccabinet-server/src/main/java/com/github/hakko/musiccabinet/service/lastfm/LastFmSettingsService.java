package com.github.hakko.musiccabinet.service.lastfm;

import java.util.List;
import java.util.Locale;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

/*
 * Keeps track of common settings for last.fm update services.
 */
public class LastFmSettingsService {

	private Locale locale = Locale.ENGLISH;
	private String lastFmUsername;
	private List<LastFmUser> lastFmUsers;
	
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	public String getLastFmUsername() {
		return lastFmUsername;
	}
	
	public void setLastFmUsername(String lastFmUsername) {
		this.lastFmUsername = lastFmUsername;
	}
	
	public List<LastFmUser> getLastFmUsers() {
		return lastFmUsers;
	}
	
	public void setLastFmUsers(List<LastFmUser> lastFmUsers) {
		this.lastFmUsers = lastFmUsers;
	}
	
}
