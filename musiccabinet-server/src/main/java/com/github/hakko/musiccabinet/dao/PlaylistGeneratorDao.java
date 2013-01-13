package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.PlaylistItem;

public interface PlaylistGeneratorDao {

	// These methods assume a populated database.
	// Appropriate ws calls should be made beforehand.
	
	List<Integer> getTopTracksForArtist(int artistId, int totalCount);
	List<PlaylistItem> getPlaylistForTrack(int trackId);
	List<PlaylistItem> getPlaylistForArtist(int artistId, int artistCount, int totalCount);
	List<PlaylistItem> getPlaylistForTags(String[] tags, int artistCount, int totalCount);
	List<PlaylistItem> getPlaylistForGroup(String lastFmGroup, int artistCount, int totalCount);
	List<Integer> getPlaylistForRelatedArtists(int artistId, int artistCount, int totalCount);

	void setAllowedTrackLengthInterval(int minLength, int maxLength);
	void updateSearchIndex();
	boolean isSearchIndexCreated();
	
}
