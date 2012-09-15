package com.github.hakko.musiccabinet.service;

import java.util.List;

import com.github.hakko.musiccabinet.dao.ArtistRecommendationDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;

/*
 * Expose services related to recommending artists.
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

	public List<ArtistRecommendation> getGroupArtistsInLibrary(
			String lastFmGroupName, int offset, int length, boolean onlyAlbumArtists) {
		return dao.getGroupArtistsInLibrary(lastFmGroupName, offset, length, onlyAlbumArtists);
	}
	
	public List<String> getGroupArtistsNotInLibrary(
			String lastFmGroupName, int amount, boolean onlyAlbumArtists) {
		return dao.getGroupArtistsNotInLibrary(lastFmGroupName, amount, onlyAlbumArtists);
	}

	public List<ArtistRecommendation> getRecommendedArtistsInLibrary(
			String lastFmUsername, int offset, int limit, boolean onlyAlbumArtists) {
		return dao.getRecommendedArtistsInLibrary(lastFmUsername, offset, limit, onlyAlbumArtists);
	}

	public List<String> getRecommendedArtistsNotInLibrary(
			String lastFmUsername, int amount, boolean onlyAlbumArtists) {
		return dao.getRecommendedArtistsNotInLibrary(lastFmUsername, amount, onlyAlbumArtists);
	}

	// Spring setters
	
	public void setArtistRecommendationDao(ArtistRecommendationDao artistRecommendationDao) {
		this.dao = artistRecommendationDao;
	}
	
}