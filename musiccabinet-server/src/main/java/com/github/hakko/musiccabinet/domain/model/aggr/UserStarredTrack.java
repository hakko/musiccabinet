package com.github.hakko.musiccabinet.domain.model.aggr;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class UserStarredTrack {

	private LastFmUser lastFmUser;
	private Track starredTrack;

	public UserStarredTrack(LastFmUser lastFmUser, Track starredTrack) {
		this.lastFmUser = lastFmUser;
		this.starredTrack = starredTrack;
	}

	public LastFmUser getLastFmUser() {
		return lastFmUser;
	}

	public void setLastFmUser(LastFmUser lastFmUser) {
		this.lastFmUser = lastFmUser;
	}

	public Track getStarredTrack() {
		return starredTrack;
	}

	public void setStarredTrack(Track starredTrack) {
		this.starredTrack = starredTrack;
	}

}
