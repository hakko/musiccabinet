package com.github.hakko.musiccabinet.service;

import java.util.List;

import com.github.hakko.musiccabinet.dao.LibraryBrowserDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.LibraryStatistics;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

/*
 */
public class LibraryBrowserService {

	protected LibraryBrowserDao libraryBrowserDao;
	
	public boolean hasArtists() {
		return libraryBrowserDao.hasArtists();
	}
	
	public List<Artist> getArtists() {
		return libraryBrowserDao.getArtists();
	}
	
	public List<Artist> getArtists(int indexLetter) {
		return libraryBrowserDao.getArtists(indexLetter);
	}

	public List<Artist> getArtists(String tag, int treshold) {
		return libraryBrowserDao.getArtists(tag, treshold);
	}
	
	public List<ArtistRecommendation> getRecentlyPlayedArtists(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getRecentlyPlayedArtists(lastFmUsername, offset, limit, query);
	}

	public List<ArtistRecommendation> getMostPlayedArtists(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getMostPlayedArtists(lastFmUsername, offset, limit, query);
	}

	public List<ArtistRecommendation> getRandomArtists(int limit) {
		return libraryBrowserDao.getRandomArtists(limit);
	}

	public List<ArtistRecommendation> getStarredArtists(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getStarredArtists(lastFmUsername, offset, limit, query);
	}

	public Album getAlbum(int albumId) {
		return libraryBrowserDao.getAlbum(albumId);
	}

	public List<Album> getAlbums(int artistId, boolean sortAscending) {
		return getAlbums(artistId, true, sortAscending);
	}

	public List<Album> getAlbums(int artistId, boolean sortByYear, boolean sortAscending) {
		return libraryBrowserDao.getAlbums(artistId, sortByYear, sortAscending);
	}

	public List<Album> getRecentlyAddedAlbums(int offset, int limit, String query) {
		return libraryBrowserDao.getRecentlyAddedAlbums(offset, limit, query);
	}

	public List<Album> getRecentlyPlayedAlbums(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getRecentlyPlayedAlbums(lastFmUsername, offset, limit, query);
	}

	public List<Album> getMostPlayedAlbums(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getMostPlayedAlbums(lastFmUsername, offset, limit, query);
	}

	public List<Album> getRandomAlbums(int limit) {
		return libraryBrowserDao.getRandomAlbums(limit);
	}

	public List<Album> getStarredAlbums(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getStarredAlbums(lastFmUsername, offset, limit, query);
	}

	public List<Track> getTracks(List<Integer> trackIds) {
		return libraryBrowserDao.getTracks(trackIds);
	}
	
	public List<Integer> getRecentlyPlayedTrackIds(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getRecentlyPlayedTrackIds(lastFmUsername, offset, limit, query);
	}

	public List<Integer> getMostPlayedTrackIds(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getMostPlayedTrackIds(lastFmUsername, offset, limit, query);
	}

	public List<Integer> getStarredTrackIds(String lastFmUsername, int offset, int limit, String query) {
		return libraryBrowserDao.getStarredTrackIds(lastFmUsername, offset, limit, query);
	}

	public List<Integer> getRandomTrackIds(int limit) {
		return libraryBrowserDao.getRandomTrackIds(limit);
	}

	public List<Integer> getRandomTrackIds(int limit, Integer fromYear, Integer toYear, String genre) {
		return libraryBrowserDao.getRandomTrackIds(limit, fromYear, toYear, genre);
	}

	public String getCoverArtFileForTrack(int trackId) {
		return libraryBrowserDao.getCoverArtFileForTrack(trackId);
	}
	
	public String getLyricsForTrack(int trackId) {
		return libraryBrowserDao.getLyricsForTrack(trackId);
	}

	public List<Integer> getArtistIndexes() {
		return libraryBrowserDao.getArtistIndexes();
	}
	
	public LibraryStatistics getStatistics() {
		return libraryBrowserDao.getStatistics();
	}
	
	public int getTrackId(String filename) {
		return libraryBrowserDao.getTrackId(filename);
	}

	public void markAllFilesForFullRescan() {
		libraryBrowserDao.markAllFilesForFullRescan();
	}

	// Spring setters

	public void setLibraryBrowserDao(LibraryBrowserDao libraryBrowserDao) {
		this.libraryBrowserDao = libraryBrowserDao;
	}
	
}