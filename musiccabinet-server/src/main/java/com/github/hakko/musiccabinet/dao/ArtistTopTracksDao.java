package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public interface ArtistTopTracksDao {

	void createTopTracks(Artist artist, List<Track> topTracks);
	List<Track> getTopTracks(Artist artist);
	List<Track> getTopTracks(int artistId);

}