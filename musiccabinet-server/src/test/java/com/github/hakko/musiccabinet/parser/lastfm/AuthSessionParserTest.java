package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class AuthSessionParserTest {
	
	private static final String AUTH_SESSION_FILE = 
		"last.fm/xml/auth.getSession.xml";

	// constant values below are copied from file above
	
	private static final String NAME = "MyLastFMUsername";
	private static final String KEY = "d580d57f32848f5dcf574d1ce18d78b2";
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		AuthSessionParser parser = new AuthSessionParserImpl(
				new ResourceUtil(AUTH_SESSION_FILE).getInputStream());

		LastFmUser lastFmUser = parser.getLastFmUser();

		assertNotNull(lastFmUser);
		assertEquals(NAME, lastFmUser.getLastFmUsername());
		assertEquals(KEY, lastFmUser.getSessionKey());
	}

}