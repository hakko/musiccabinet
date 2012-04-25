package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.ArtistRelation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class ArtistSimilarityParserTest {
	
	private static final String ARTIST_SIMILARITY_FILE = 
		"last.fm/xml/similarartists.cher.xml";

	@Test
	public void testdataOnClasspath() {
		new ResourceUtil(ARTIST_SIMILARITY_FILE);
	}
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		ArtistSimilarityParser parser = new ArtistSimilarityParserImpl(
				new ResourceUtil(ARTIST_SIMILARITY_FILE).getInputStream());
	
		assertNotNull(parser.getArtist());
		assertNotNull(parser.getArtistRelations());

		assertTrue(parser.getArtist().getName().equals("Cher"));
		
		assertEquals(parser.getArtistRelations().size(), 100);
		for (ArtistRelation ar : parser.getArtistRelations()) {
			assertNotNull(ar.getTarget());
		}
		
		verifyArtistRelation(parser, 0, "Sonny & Cher", 1.0f);
		verifyArtistRelation(parser, 1, "Madonna", 0.476751f);
		verifyArtistRelation(parser, 2, "Cyndi Lauper", 0.407297f);
		verifyArtistRelation(parser, 99, "Lara Fabian", 0.0617754f);
	}
	
	private void verifyArtistRelation(ArtistSimilarityParser parser, 
			int artistRelationIndex, String artistName, float match) {
		ArtistRelation relation = parser.getArtistRelations().get(artistRelationIndex);
		assertTrue(relation.getTarget().getName().equals(artistName));
		assertEquals(relation.getMatch(), match);
	}
	
}