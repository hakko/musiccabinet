package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.TrackRelation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class TrackSimilarityParserTest {
	
	private static final String TRACK_SIMILARITY_FILE = 
		"last.fm/xml/similartracks.cher.believe.xml";

	@Test
	public void testdataOnClasspath() {
		new ResourceUtil(TRACK_SIMILARITY_FILE);
	}
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		TrackSimilarityParser parser = new TrackSimilarityParserImpl(
				new ResourceUtil(TRACK_SIMILARITY_FILE).getInputStream());
	
		assertNotNull(parser.getTrack());
		assertNotNull(parser.getTrackRelations());

		assertTrue(parser.getTrack().getArtist().getName().equals("Cher"));
		assertTrue(parser.getTrack().getName().equals("Believe"));
		
		assertEquals(parser.getTrackRelations().size(), 250);
		for (TrackRelation tr : parser.getTrackRelations()) {
			assertNotNull(tr.getTarget());
			assertNotNull(tr.getTarget().getArtist());
		}
	
		verifyTrackRelation(parser, 0, "Cher", "Strong Enough", 1.0f);
		verifyTrackRelation(parser, 1, "Cher", "All Or Nothing", 0.961879f);
		verifyTrackRelation(parser, 2, "Madonna", "Vogue", 0.291088f);
	}
	
	private void verifyTrackRelation(TrackSimilarityParser parser, 
			int trackRelationIndex, String artistName, String trackName, float match) {
		TrackRelation relation = parser.getTrackRelations().get(trackRelationIndex);
		assertTrue(relation.getTarget().getArtist().getName().equals(artistName));
		assertTrue(relation.getTarget().getName().equals(trackName));
		assertEquals(relation.getMatch(), match);
	}
	
}