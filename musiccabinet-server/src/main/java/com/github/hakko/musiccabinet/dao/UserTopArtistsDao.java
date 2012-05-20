package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.UserTopArtists;
import com.github.hakko.musiccabinet.domain.model.library.Period;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

public interface UserTopArtistsDao {

	void createUserTopArtists(List<UserTopArtists> userTopArtists);
	List<ArtistRecommendation> getUserTopArtists(LastFmUser user, Period period, int offset, int limit);
	
}