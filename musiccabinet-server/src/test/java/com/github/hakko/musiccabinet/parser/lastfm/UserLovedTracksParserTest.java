package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class UserLovedTracksParserTest {
	
	private static final String LOVED_TRACKS_FILE = 
		"last.fm/xml/userlovedtracks.rj.xml";
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		UserLovedTracksParser parser = new UserLovedTracksParserImpl(
				new ResourceUtil(LOVED_TRACKS_FILE).getInputStream());

		assertNotNull(parser.getLovedTracks());

		assertEquals(parser.getPage(), (short) 1);
		assertEquals(parser.getTotalPages(), (short) 13);

		assertEquals(parser.getLovedTracks().size(), 50);

		verifyUserLovedTrack(parser, 0, "Paul Hardcastle", "Desire");
		verifyUserLovedTrack(parser, 1, "Zomboy", "Organ Donor");
		verifyUserLovedTrack(parser, 2, "Jens Lekman", "An Argument With Myself");
		verifyUserLovedTrack(parser, 49, "Frank Ocean", "Lost");
	}

	private void verifyUserLovedTrack(UserLovedTracksParser parser, 
			int trackIndex, String artistName, String trackName) {
		Track lovedTrack = parser.getLovedTracks().get(trackIndex);
		assertTrue(artistName.equals(lovedTrack.getArtist().getName()));
		assertTrue(trackName.equals(lovedTrack.getName()));
	}
	
}