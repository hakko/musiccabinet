package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.NameSearchResult;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.SearchCriteria;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public interface NameSearchDao {
	
	NameSearchResult<Artist> getArtists(String userQuery, int offset, int limit);
	NameSearchResult<Album> getAlbums(String userQuery, int offset, int limit);
	NameSearchResult<Track> getTracks(String userQuery, int offset, int limit);
	List<Track> getTracks(SearchCriteria searchCriteria, int offset, int limit);
	
}
