package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.UserLovedTracks;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public interface UserLovedTracksDao {

	void createLovedTracks(List<UserLovedTracks> userLovedTracks);
	List<Track> getLovedTracks(String lastFmUsername);

}