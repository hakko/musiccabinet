package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.ArtistPlayCount;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class GroupWeeklyArtistChartParserTest {
	
	private static final String ARTIST_CHART_FILE = 
		"last.fm/xml/group.weeklyartistchart.xml";

	private static final List<ArtistPlayCount> EXPECTED_PLAY_COUNTS = Arrays.asList(
			new ArtistPlayCount("Swans", 88),
			new ArtistPlayCount("Coil", 51),
			new ArtistPlayCount("Current 93", 43),
			new ArtistPlayCount("Dead Can Dance", 41),
			new ArtistPlayCount("Four Tet", 38)
			);
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		GroupWeeklyArtistChartParser parser = new GroupWeeklyArtistChartParserImpl(
				new ResourceUtil(ARTIST_CHART_FILE).getInputStream());
		List<ArtistPlayCount> playCounts = parser.getArtistPlayCount();
		
		for (int i = 0; i < EXPECTED_PLAY_COUNTS.size(); i++) {
			assertEquals(EXPECTED_PLAY_COUNTS.get(i), playCounts.get(i));
		}
		
		Assert.assertEquals(100, playCounts.size());
	}

}