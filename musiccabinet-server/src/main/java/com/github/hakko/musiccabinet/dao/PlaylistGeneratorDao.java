package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.PlaylistItem;

public interface PlaylistGeneratorDao {

	/* These methods assume a populated database.
	 * Appropriate ws calls should be made beforehand.
	 */
	List<PlaylistItem> getPlaylistForTrack(int trackId);
	List<PlaylistItem> getPlaylistForArtist(int artistId);
	List<String> getTopTracksForArtist(int artistId);
	
	List<String> getPlaylistForTags(String[] tags);
	
	void updateSearchIndex();
	boolean isSearchIndexCreated();
	
}
