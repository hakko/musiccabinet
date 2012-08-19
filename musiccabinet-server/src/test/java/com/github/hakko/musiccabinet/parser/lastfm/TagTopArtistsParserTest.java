package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class TagTopArtistsParserTest {
	
	private static final String DISCO_TOP_ARTISTS_FILE = 
		"last.fm/xml/tagtopartists.disco.xml";

	private static final List<String> EXPECTED_ARTISTS = Arrays.asList(
			"Bee Gees",
			"Donna Summer",
			"ABBA",
			"Boney M.",
			"KC And The Sunshine Band",
			"Chic",
			"Electric Six",
			"Kool & The Gang",
			"Earth, Wind & Fire",
			"Village People"
			);
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		TagTopArtistsParser parser = new TagTopArtistsParserImpl(
				new ResourceUtil(DISCO_TOP_ARTISTS_FILE).getInputStream());
		List<Artist> artists = parser.getArtists();
		
		for (int i = 0; i < EXPECTED_ARTISTS.size(); i++) {
			assertEquals(EXPECTED_ARTISTS.get(i), artists.get(i).getName());
		}
		
		Assert.assertEquals(50, artists.size());
	}

}