package com.github.hakko.musiccabinet.parser.lastfm;

import java.io.InputStream;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.AbstractSAXParserImpl;

public class AuthSessionParserImpl extends AbstractSAXParserImpl implements AuthSessionParser {
	
	private AuthSessionHandler handler = new AuthSessionHandler();

	public AuthSessionParserImpl(InputStream source) throws ApplicationException {
		parseFromStream(source, handler);
	}

	@Override
	public LastFmUser getLastFmUser() {
		return handler.getLastFmUser();
	}
	
}