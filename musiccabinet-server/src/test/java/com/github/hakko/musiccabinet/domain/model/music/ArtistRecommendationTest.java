package com.github.hakko.musiccabinet.domain.model.music;

import junit.framework.Assert;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;

public class ArtistRecommendationTest {

	@Test
	public void validateConstructor() {
		String artistName = "Madonna";
		String path = "/path/to/madonna";
		ArtistRecommendation ar = new ArtistRecommendation(artistName, path);
		
		Assert.assertEquals(new Artist(artistName), new Artist(ar.getArtistName()));
		Assert.assertEquals(ar.getPath(), path);
	}
	
}