package com.github.hakko.musiccabinet.service;

import java.util.List;

import com.github.hakko.musiccabinet.dao.LibraryBrowserDao;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

/*
 */
public class LibraryBrowserService {

	protected LibraryBrowserDao libraryBrowserDao;
	
	public List<Artist> getArtists() {
		return libraryBrowserDao.getArtists();
	}
	
	public List<Album> getAlbums(int artistId) {
		return libraryBrowserDao.getAlbums(artistId);
	}
	
	public List<Track> getTracks(int albumId) {
		return libraryBrowserDao.getTracks(albumId);
	}
	
	// Spring setters

	public void setLibraryBrowserDao(LibraryBrowserDao libraryBrowserDao) {
		this.libraryBrowserDao = libraryBrowserDao;
	}
	
}