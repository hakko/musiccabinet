package com.github.hakko.musiccabinet.parser.musicbrainz;

import java.io.InputStream;

import com.github.hakko.musiccabinet.domain.model.music.MBArtist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class ArtistQueryParserImpl extends AbstractSAXParserImpl implements
		ArtistQueryParser {

	private ArtistQueryHandler handler = new ArtistQueryHandler();

	public ArtistQueryParserImpl(InputStream source)
			throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public MBArtist getArtist() {
		return handler.getArtist();
	}

}