package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class ArtistTopTracksParserTest {
	
	private static final String TOP_TRACK_FILE = 
		"last.fm/xml/toptracks.cher.xml";
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		ArtistTopTracksParser parser = new ArtistTopTracksParserImpl(
				new ResourceUtil(TOP_TRACK_FILE).getInputStream());
	
		assertNotNull(parser.getArtist());
		assertNotNull(parser.getTopTracks());

		assertTrue(parser.getArtist().getName().equals("Cher"));
		
		assertEquals(parser.getTopTracks().size(), 50);
		for (Track track : parser.getTopTracks()) {
			assertNotNull(track);
			assertNotNull(track.getArtist());
			assertEquals(track.getArtist(), parser.getArtist());
		}
	
		verifyTopTrack(parser, 0, "Believe");
		verifyTopTrack(parser, 1, "If I Could Turn Back Time");
		verifyTopTrack(parser, 2, "Welcome To Burlesque");
		verifyTopTrack(parser, 49, "Gypsys, Tramps and Thieves");
	}
	
	private void verifyTopTrack(ArtistTopTracksParser parser, 
			int trackIndex, String trackName) {
		Track track = parser.getTopTracks().get(trackIndex);
		assertTrue(track.getName().equals(trackName));
	}
	
}