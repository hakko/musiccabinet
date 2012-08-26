package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.aggr.UserRecommendedArtists.RecommendedArtist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class UserRecommendedArtistsParserImpl extends AbstractSAXParserImpl implements UserRecommendedArtistsParser {
	
	private UserRecommendedArtistsHandler handler = new UserRecommendedArtistsHandler();

	public UserRecommendedArtistsParserImpl(InputStream source) throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public List<RecommendedArtist> getArtists() {
		return handler.getArtists();
	}
	
}