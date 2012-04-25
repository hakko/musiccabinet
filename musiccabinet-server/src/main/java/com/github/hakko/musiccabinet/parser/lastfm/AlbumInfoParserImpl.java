package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;

import com.github.hakko.musiccabinet.domain.model.music.AlbumInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class AlbumInfoParserImpl extends AbstractSAXParserImpl implements AlbumInfoParser {
	
	private AlbumInfoHandler handler = new AlbumInfoHandler();

	public AlbumInfoParserImpl(InputStream source) throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public AlbumInfo getAlbumInfo() {
		return handler.getAlbumInfo();
	}
	
}