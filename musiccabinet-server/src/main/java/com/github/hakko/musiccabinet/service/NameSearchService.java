package com.github.hakko.musiccabinet.service;

import java.util.List;

import com.github.hakko.musiccabinet.dao.NameSearchDao;
import com.github.hakko.musiccabinet.domain.model.aggr.NameSearchResult;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.SearchCriteria;
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

	public List<Integer> getTracks(SearchCriteria searchCriteria, int offset, int limit) {
		return nameSearchDao.getTrackIds(searchCriteria, offset, limit);
	}

	public List<String> getFileTypes() {
		return nameSearchDao.getFileTypes();
	}

	// Spring setter
	
	public void setNameSearchDao(NameSearchDao nameSearchDao) {
		this.nameSearchDao = nameSearchDao;
	}

}