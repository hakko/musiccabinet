package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class ArtistTopTagsParserTest {
	
	private static final String TOP_TAGS_FILE = 
		"last.fm/xml/toptags.cher.xml";
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		ArtistTopTagsParser parser = new ArtistTopTagsParserImpl(
				new ResourceUtil(TOP_TAGS_FILE).getInputStream());
	
		assertNotNull(parser.getArtist());
		assertNotNull(parser.getTopTags());

		assertTrue(parser.getArtist().getName().equals("Cher"));
		
		assertEquals(parser.getTopTags().size(), 100);
		for (Tag tag : parser.getTopTags()) {
			assertNotNull(tag);
			assertNotNull(tag.getName());
		}
	
		verifyTopTag(parser, 0, "pop", 100);
		verifyTopTag(parser, 1, "female vocalists", 72);
		verifyTopTag(parser, 98, "women", 0);
		verifyTopTag(parser, 99, "techno", 0);
	}
	
	private void verifyTopTag(ArtistTopTagsParser parser, 
			int tagIndex, String tagName, int tagCount) {
		Tag tag = parser.getTopTags().get(tagIndex);
		assertTrue(tag.getName().equals(tagName));
		assertEquals(tagCount, tag.getCount());
	}
	
}