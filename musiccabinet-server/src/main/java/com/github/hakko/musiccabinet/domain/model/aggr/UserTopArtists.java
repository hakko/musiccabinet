package com.github.hakko.musiccabinet.domain.model.aggr;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.library.Period;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;

public class UserTopArtists {

	private LastFmUser user;
	private Period period;
	private List<Artist> artists;
	
	public UserTopArtists(LastFmUser user, Period period, List<Artist> artists) {
		this.user = user;
		this.period = period;
		this.artists = artists;
	}

	public LastFmUser getUser() {
		return user;
	}

	public void setUser(LastFmUser user) {
		this.user = user;
	}

	public Period getPeriod() {
		return period;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

	public List<Artist> getArtists() {
		return artists;
	}

	public void setArtists(List<Artist> artists) {
		this.artists = artists;
	}
	
}