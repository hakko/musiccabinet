package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public interface PlayCountDao {

	void addPlayCount(LastFmUser lastFmUser, Track track);

	List<Integer> getRecentArtists(String lastFmUser, int offset, int limit);
	List<Integer> getRecentAlbums(String lastFmUser, int offset, int limit);
	List<Integer> getRecentTracks(String lastFmUser, int offset, int limit);
	
	List<Integer> getMostPlayedArtists(String lastFmUser, int offset, int limit);
	List<Integer> getMostPlayedAlbums(String lastFmUser, int offset, int limit);
	List<Integer> getMostPlayedTracks(String lastFmUser, int offset, int limit);
}