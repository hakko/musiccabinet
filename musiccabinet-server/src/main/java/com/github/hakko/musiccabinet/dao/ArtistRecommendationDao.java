package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;

public interface ArtistRecommendationDao {

	List<ArtistRecommendation> getRecommendedArtistsInLibrary(int artistId, int amount);
	List<String> getRecommendedArtistsNotInLibrary(int artistId, int amount);
	int getNumberOfRelatedSongs(int artistId);
	List<ArtistRecommendation> getRecommendedArtistsFromGenre(String tagName, int offset, int length);

}
