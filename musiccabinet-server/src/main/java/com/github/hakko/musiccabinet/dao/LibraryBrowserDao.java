package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.LibraryStatistics;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

public interface LibraryBrowserDao {

	boolean hasArtists();
	List<Artist> getArtists();
	List<Artist> getArtists(int indexLetter);
	List<Artist> getArtists(String tag, int treshold);
	List<ArtistRecommendation> getRecentlyPlayedArtists(String lastFmUsername, boolean onlyAlbumArtists, int offset, int limit, String query);
	List<ArtistRecommendation> getMostPlayedArtists(String lastFmUsername, int offset, int limit, String query);
	List<ArtistRecommendation> getRandomArtists(boolean onlyAlbumArtists, int limit);
	List<ArtistRecommendation> getStarredArtists(String lastFmUsername, int offset, int limit, String query);

	Album getAlbum(int albumId);
	List<Album> getAlbums(int artistId, boolean sortAscending);
	List<Album> getAlbums(int artistId, boolean sortByYear, boolean sortAscending);
	List<Album> getVariousArtistsAlbums();
	List<Album> getRecentlyAddedAlbums(int offset, int limit, String query);
	List<Album> getRecentlyPlayedAlbums(String lastFmUsername, int offset, int limit, String query);
	List<Album> getMostPlayedAlbums(String lastFmUsername, int offset, int limit, String query);
	List<Album> getRandomAlbums(int limit);
	List<Album> getStarredAlbums(String lastFmUsername, int offset, int limit, String query);

	Track getTrack(int trackId);
	List<Track> getTracks(List<Integer> trackIds);
	List<Integer> getRecentlyPlayedTrackIds(String lastFmUsername, int offset, int limit, String query);
	List<Integer> getMostPlayedTrackIds(String lastFmUsername, int offset, int limit, String query);
	List<Integer> getStarredTrackIds(String lastFmUsername, int offset, int limit, String query);
	List<Integer> getRandomTrackIds(int limit);
	List<Integer> getRandomTrackIds(int limit, Integer fromYear, Integer toYear, String genre);
	
	String getCoverArtFileForTrack(int trackId);
	void addArtwork(List<Track> tracks);
	String getLyricsForTrack(int trackId);
	String getLyricsForTrack(String artistName, String trackName);

	List<Integer> getArtistIndexes();
	LibraryStatistics getStatistics();
	
	int getTrackId(String filename);
	
	void markAllFilesForFullRescan();
	List<String> getFilesMissingMetadata();
	
}