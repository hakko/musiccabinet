package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.aggr.UserRecommendedArtists.RecommendedArtist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.util.StringUtil;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

public class UserRecommendedArtistsParserTest {
	
	private static final String USER_RECOMMENDED_ARTISTS_FILE = 
		"last.fm/xml/userrecommendedartists.joanofarctan.xml";

	private static final String USER_RECOMMENDED_ARTISTS_FILE2 = 
			"last.fm/xml/userrecommendedartists.ftparea.xml";

	private static final List<String> EXPECTED_ARTISTS1 = Arrays.asList(
			"Quest.Room.Project",
			"Senior Soul"
			);

	private static final List<RecommendedArtist> EXPECTED_ARTISTS2 = Arrays.asList(
			new RecommendedArtist("Snowgoons", "Outerspace", "Army of the Pharaohs"),
			new RecommendedArtist("Diabolic", "Ill Bill & Vinnie Paz", "Outerspace"),
			new RecommendedArtist("Slaine", "Ill Bill & Vinnie Paz", "Ill Bill"),
			new RecommendedArtist("Sabac", "Outerspace", "Ill Bill & Vinnie Paz"),
			new RecommendedArtist("Randam Luck", "Outerspace", "King Syze")
			);

	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		UserRecommendedArtistsParser parser = new UserRecommendedArtistsParserImpl(
				new ResourceUtil(USER_RECOMMENDED_ARTISTS_FILE).getInputStream());
		List<RecommendedArtist> artists = parser.getArtists();
		
		for (int i = 0; i < EXPECTED_ARTISTS1.size(); i++) {
			assertEquals(EXPECTED_ARTISTS1.get(i), artists.get(i).getArtist().getName());
		}
	}

	@Test
	public void resourceFile2CorrectlyParsed() throws ApplicationException {
		WSResponse wsResponse = new WSResponse(new ResourceUtil(
				USER_RECOMMENDED_ARTISTS_FILE2).getContent());
		
		UserRecommendedArtistsParser parser = new UserRecommendedArtistsParserImpl(
				new StringUtil(wsResponse.getResponseBody()).getInputStream());
		List<RecommendedArtist> artists = parser.getArtists();
		
		for (int i = 0; i < EXPECTED_ARTISTS2.size(); i++) {
			assertEquals(EXPECTED_ARTISTS2.get(i), artists.get(i));
		}
	}

}