package com.github.hakko.musiccabinet.parser.lastfm;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.UserRecommendedArtists.RecommendedArtist;

public interface UserRecommendedArtistsParser {

	List<RecommendedArtist> getArtists();
	
}
