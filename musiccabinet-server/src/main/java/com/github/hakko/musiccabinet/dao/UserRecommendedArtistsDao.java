package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.UserRecommendedArtists;
import com.github.hakko.musiccabinet.domain.model.aggr.UserRecommendedArtists.RecommendedArtist;

public interface UserRecommendedArtistsDao {

	void createUserRecommendedArtists(List<UserRecommendedArtists> userRecommendedArtists);
	List<RecommendedArtist> getUserRecommendedArtists(String lastFmUsername);
	
}