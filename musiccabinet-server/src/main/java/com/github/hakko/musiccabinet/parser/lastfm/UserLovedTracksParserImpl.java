package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;
import java.util.List;

import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class UserLovedTracksParserImpl extends AbstractSAXParserImpl implements UserLovedTracksParser {
	
	private UserLovedTracksHandler handler = new UserLovedTracksHandler();
	
	public UserLovedTracksParserImpl(InputStream source) throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public List<Track> getLovedTracks() {
		return handler.getLovedTracks();
	}

	@Override
	public short getPage() {
		return handler.getPage();
	}
	
	@Override
	public short getTotalPages() {
		return handler.getTotalPages();
	}
	
}