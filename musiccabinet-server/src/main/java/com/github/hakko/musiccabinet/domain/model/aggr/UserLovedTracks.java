package com.github.hakko.musiccabinet.domain.model.aggr;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Track;

public class UserLovedTracks {

	private String lastFmUsername;
	private List<Track> lovedTracks;

	public UserLovedTracks(String lastFmUsername, List<Track> lovedTracks) {
		this.lastFmUsername = lastFmUsername;
		this.lovedTracks = lovedTracks;
	}

	public String getLastFmUsername() {
		return lastFmUsername;
	}

	public void setLastFmUsername(String lastFmUsername) {
		this.lastFmUsername = lastFmUsername;
	}

	public List<Track> getLovedTracks() {
		return lovedTracks;
	}

	public void setLovedTracks(List<Track> lovedTracks) {
		this.lovedTracks = lovedTracks;
	}
		
}
