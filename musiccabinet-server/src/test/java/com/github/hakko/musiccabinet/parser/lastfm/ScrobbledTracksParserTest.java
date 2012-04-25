package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.library.TrackPlayCount;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class ScrobbledTracksParserTest {
	
	private static final String LIBRARY_TRACKS_FILE = 
		"last.fm/xml/tracks.ftparea.xml";

	@Test
	public void testdataOnClasspath() {
		new ResourceUtil(LIBRARY_TRACKS_FILE);
	}
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		ScrobbledTracksParser parser = new ScrobbledTracksParserImpl(
				new ResourceUtil(LIBRARY_TRACKS_FILE).getInputStream());
	
		assertNotNull(parser.getTrackPlayCounts());

		assertEquals(parser.getPage(), (short) 1);
		assertEquals(parser.getTotalPages(), (short) 151);
		
		assertEquals(parser.getTrackPlayCounts().size(), 100);
		for (TrackPlayCount trackPlayCount : parser.getTrackPlayCounts()) {
			assertNotNull(trackPlayCount.getTrack());
			assertNotNull(trackPlayCount.getTrack().getArtist());
		}
	
		verifyLibraryTrack(parser, 0, "Can", "Thief", 181);
		verifyLibraryTrack(parser, 1, "Roffe Ruff", "Ormar i gräset", 144);
		verifyLibraryTrack(parser, 2, "Dungen", "Du E för Fin för Mig", 143);
		verifyLibraryTrack(parser, 99, "The Radio Dept.", "Gibraltar", 41);
	}
	
	private void verifyLibraryTrack(ScrobbledTracksParser parser, 
			int libraryTrackIndex, String artistName, String trackName, int playCount) {
		TrackPlayCount trackPlayCount = parser.getTrackPlayCounts().get(libraryTrackIndex);
		assertTrue(artistName.equals(trackPlayCount.getTrack().getArtist().getName()));
		assertTrue(trackName.equals(trackPlayCount.getTrack().getName()));
		assertEquals(playCount, trackPlayCount.getPlayCount());
	}
	
}