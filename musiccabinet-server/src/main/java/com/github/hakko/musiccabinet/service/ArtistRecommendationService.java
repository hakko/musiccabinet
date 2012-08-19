package com.github.hakko.musiccabinet.service;

import java.util.List;

import com.github.hakko.musiccabinet.dao.ArtistRecommendationDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;

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

	protected ArtistRecommendationDao dao;
	
	public List<ArtistRecommendation> getRelatedArtistsInLibrary(
			int artistId, int amount, boolean onlyAlbumArtists) {
		return dao.getRelatedArtistsInLibrary(artistId, amount, onlyAlbumArtists);
	}
	
	public List<String> getRelatedArtistsNotInLibrary(
			int artistId, int amount, boolean onlyAlbumArtists) {
		return dao.getRelatedArtistsNotInLibrary(artistId, amount, onlyAlbumArtists);
	}

	public List<ArtistRecommendation> getGenreArtistsInLibrary(
			String tagName, int offset, int length, boolean onlyAlbumArtists) {
		return dao.getGenreArtistsInLibrary(tagName, offset, length, onlyAlbumArtists);
	}

	public List<String> getGenreArtistsNotInLibrary(
			String tagName, int amount, boolean onlyAlbumArtists) {
		return dao.getGenreArtistsNotInLibrary(tagName, amount, onlyAlbumArtists);
	}

	// Spring setters
	
	public void setArtistRecommendationDao(ArtistRecommendationDao artistRecommendationDao) {
		this.dao = artistRecommendationDao;
	}
	
}