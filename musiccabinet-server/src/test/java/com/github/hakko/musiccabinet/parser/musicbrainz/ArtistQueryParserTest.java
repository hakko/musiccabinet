package com.github.hakko.musiccabinet.parser.musicbrainz;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.MBArtist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class ArtistQueryParserTest {

	private static final String ARTIST_QUERY_DATE_FILE =
			"musicbrainz/xml/artistQuery-date.xml";

	private static final String ARTIST_QUERY_EMPTY_FILE =
		"musicbrainz/xml/artistQuery-empty.xml";
	
	private static final String ARTIST_QUERY_FILE = 
		"musicbrainz/xml/artistQuery.xml";

	// constant values below are copied from file above
	private static final String MBID = "d347406f-839d-4423-9a28-188939282afa";
	private static final String NAME = "Cult of Luna";
	private static final String COUNTRY_CODE = "SE";
	private static final short START_YEAR = 1998;
	private static final boolean ACTIVE = true;

	// year from date 1966-06-03 in file artistQuery-date.xml
	private static final short YEAR_PART = 1966;
	
	@Test
	public void emptyResourceFileCorrectlyParsed() throws ApplicationException {
		ArtistQueryParser parser = new ArtistQueryParserImpl(
				new ResourceUtil(ARTIST_QUERY_EMPTY_FILE).getInputStream());
		
		MBArtist artist = parser.getArtist();
		
		assertNull(artist);
	}

	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		ArtistQueryParser parser = new ArtistQueryParserImpl(
				new ResourceUtil(ARTIST_QUERY_FILE).getInputStream());
		
		MBArtist artist = parser.getArtist();
		
		assertEquals(MBID, artist.getMbid());
		assertEquals(NAME, artist.getName());
		assertEquals(COUNTRY_CODE, artist.getCountryCode());
		assertEquals(START_YEAR, artist.getStartYear());
		assertEquals(ACTIVE, artist.isActive());
	}

	@Test
	public void dateGetsStoredAsYearOnly() throws ApplicationException {
		ArtistQueryParser parser = new ArtistQueryParserImpl(
				new ResourceUtil(ARTIST_QUERY_DATE_FILE).getInputStream());
		
		MBArtist artist = parser.getArtist();
		
		assertEquals(YEAR_PART, artist.getStartYear());
	}

}