package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class TagTopArtistsParserImpl extends AbstractSAXParserImpl implements TagTopArtistsParser {
	
	private TagTopArtistsHandler handler = new TagTopArtistsHandler();

	public TagTopArtistsParserImpl(InputStream source) throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public List<Artist> getArtists() {
		return handler.getArtists();
	}
	
}