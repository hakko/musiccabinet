package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

public interface StarDao {

	void starArtist(LastFmUser lastFmUser, int artistId);
	void unstarArtist(LastFmUser lastFmUser, int artistId);
	List<Integer> getStarredArtistIds(LastFmUser lastFmUser, int offset, int limit, String query);

	void starAlbum(LastFmUser lastFmUser, int albumId);
	void unstarAlbum(LastFmUser lastFmUser, int albumId);
	List<Integer> getStarredAlbumIds(LastFmUser lastFmUser, int offset, int limit, String query);

	void starTrack(LastFmUser lastFmUser, int trackId);
	void unstarTrack(LastFmUser lastFmUser, int trackId);
	List<Integer> getStarredTrackIds(LastFmUser lastFmUser, int offset, int limit, String query);

}