package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class UserTopArtistsParserTest {
	
	private static final String USER_TOP_ARTISTS_FILE = 
		"last.fm/xml/usertopartists.arnathalon.overall.xml";

	private static final List<String> EXPECTED_ARTISTS = Arrays.asList(
			"Belle and Sebastian",
			"The Magnetic Fields",
			"Sufjan Stevens",
			"M83",
			"bob hund",
			"Cat Power",
			"Radiohead",
			"Casiotone for the Painfully Alone",
			"TV on the Radio",
			"The Radio Dept."
			);
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		UserTopArtistsParser parser = new UserTopArtistsParserImpl(
				new ResourceUtil(USER_TOP_ARTISTS_FILE).getInputStream());
		List<Artist> artists = parser.getArtists();
		
		for (int i = 0; i < EXPECTED_ARTISTS.size(); i++) {
			assertEquals(EXPECTED_ARTISTS.get(i), artists.get(i).getName());
		}
	}

}