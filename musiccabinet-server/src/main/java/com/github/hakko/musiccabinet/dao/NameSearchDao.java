package com.github.hakko.musiccabinet.dao;

import com.github.hakko.musiccabinet.domain.model.aggr.NameSearchResult;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public interface NameSearchDao {
	
	NameSearchResult<Artist> getArtists(String userQuery, int offset, int limit);
	NameSearchResult<Album> getAlbums(String userQuery, int offset, int limit);
	NameSearchResult<Track> getTracks(String userQuery, int offset, int limit);

}
