package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;

public interface ArtistRecommendationDao {

	List<ArtistRecommendation> getRelatedArtistsInLibrary(int artistId, int amount, boolean onlyAlbumArtists);
	List<String> getRelatedArtistsNotInLibrary(int artistId, int amount, boolean onlyAlbumArtists);

	List<ArtistRecommendation> getGenreArtistsInLibrary(String tagName, int offset, int length, boolean onlyAlbumArtists);
	List<String> getGenreArtistsNotInLibrary(String tagName, int amount, boolean onlyAlbumArtists);

}
