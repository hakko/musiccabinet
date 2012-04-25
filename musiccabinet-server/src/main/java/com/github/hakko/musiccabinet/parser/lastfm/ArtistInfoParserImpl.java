package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;

import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class ArtistInfoParserImpl extends AbstractSAXParserImpl implements ArtistInfoParser {
	
	private ArtistInfoHandler handler = new ArtistInfoHandler();

	public ArtistInfoParserImpl(InputStream source) throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public ArtistInfo getArtistInfo() {
		return handler.getArtistInfo();
	}
	
}