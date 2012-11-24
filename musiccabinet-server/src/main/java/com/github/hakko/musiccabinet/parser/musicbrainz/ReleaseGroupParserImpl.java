package com.github.hakko.musiccabinet.parser.musicbrainz;

import java.io.InputStream;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class ReleaseGroupParserImpl extends AbstractSAXParserImpl implements
		ReleaseGroupParser {

	private ReleaseGroupHandler handler = new ReleaseGroupHandler();

	public ReleaseGroupParserImpl(InputStream source)
			throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public List<MBAlbum> getAlbums() {
		return handler.getAlbums();
	}
	
	@Override
	public int getTotalAlbums() {
		return handler.getTotalAlbums();
	}

}