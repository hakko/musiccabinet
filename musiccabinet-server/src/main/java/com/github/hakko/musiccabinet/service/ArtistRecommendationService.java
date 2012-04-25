package com.github.hakko.musiccabinet.service;

import java.util.List;

import com.github.hakko.musiccabinet.dao.MusicDirectoryDao;
import com.github.hakko.musiccabinet.dao.ArtistRecommendationDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

/*
 * Expose services related to recommending artists.
 * 
 * - Using the artist relations created through {@link ArtistRelationService},
 * we can pick closely related artists, also distinguishing between those already
 * present in library and those who aren't.
 * 
 * - By using artist tags and global artist playcount, we can recommend most
 * relevant artists from a given genre.
 */
public class ArtistRecommendationService {

	protected ArtistRecommendationDao artistRecommendationDao;

	protected MusicDirectoryDao musicDirectoryDao;
	
	public List<ArtistRecommendation> getRelatedArtistsInLibrary(String path, int amount) throws ApplicationException {
		int artistId = musicDirectoryDao.getArtistId(path);
		return artistRecommendationDao.getRecommendedArtistsInLibrary(artistId, amount);
	}
	
	public List<String> getRelatedArtistsNotInLibrary(String path, int amount) throws ApplicationException {
		int artistId = musicDirectoryDao.getArtistId(path);
		return artistRecommendationDao.getRecommendedArtistsNotInLibrary(artistId, amount);
	}
	
	// TODO : currently not used, should be, but display more nicely on a scale from "Poor" to "Good"
	public int getMatchingSongs(String path) throws ApplicationException {
		int artistId = musicDirectoryDao.getArtistId(path);
		return artistRecommendationDao.getNumberOfRelatedSongs(artistId);
	}

	public List<ArtistRecommendation> getRecommendedArtistsFromGenre(String tagName, int offset, int length) {
		return artistRecommendationDao.getRecommendedArtistsFromGenre(tagName, offset, length);
	}

	// Spring setters
	
	public void setArtistRecommendationDao(ArtistRecommendationDao artistRecommendationDao) {
		this.artistRecommendationDao = artistRecommendationDao;
	}

	public void setMusicDirectoryDao(MusicDirectoryDao musicDirectoryDao) {
		this.musicDirectoryDao = musicDirectoryDao;
	}
	
}