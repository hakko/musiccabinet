package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class UserTopArtistsParserImpl extends AbstractSAXParserImpl implements UserTopArtistsParser {
	
	private UserTopArtistsHandler handler = new UserTopArtistsHandler();

	public UserTopArtistsParserImpl(InputStream source) throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public List<Artist> getArtists() {
		return handler.getArtists();
	}
	
}