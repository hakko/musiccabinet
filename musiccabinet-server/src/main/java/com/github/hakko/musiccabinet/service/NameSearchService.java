package com.github.hakko.musiccabinet.service;

import com.github.hakko.musiccabinet.dao.NameSearchDao;
import com.github.hakko.musiccabinet.domain.model.aggr.NameSearchResult;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public class NameSearchService {

	private NameSearchDao nameSearchDao;
	
	public NameSearchResult<Artist> getArtists(String userQuery, int offset, int limit) {
		return nameSearchDao.getArtists(userQuery, offset, limit);
	}
	
	public NameSearchResult<Album> getAlbums(String userQuery, int offset, int limit) {
		return nameSearchDao.getAlbums(userQuery, offset, limit);
	}
	
	public NameSearchResult<Track> getTracks(String userQuery, int offset, int limit) {
		return nameSearchDao.getTracks(userQuery, offset, limit);
	}

	// Spring setter
	
	public void setNameSearchDao(NameSearchDao nameSearchDao) {
		this.nameSearchDao = nameSearchDao;
	}

}