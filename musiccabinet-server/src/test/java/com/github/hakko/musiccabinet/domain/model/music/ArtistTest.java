package com.github.hakko.musiccabinet.domain.model.music;

import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.Test;

public class ArtistTest {

	@Test
	public void shouldCreateArtist() {
		String artistName = "Cher";
		Artist artist = new Artist(artistName);
		
		assertTrue(artistName.equals(artist.getName()));
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void artistNameIsNullsafe() {
		new Artist(null);
	}

	@Test
	public void validateArtistEquality() {
		Artist a1 = new Artist("cher");
		Artist a2 = new Artist("cher");
		Artist a3 = new Artist("madonna");
		
		Assert.assertTrue(a1.equals(a1));
		Assert.assertTrue(a1.equals(a2));

		Assert.assertFalse(a1.equals(a3));
		Assert.assertFalse(a2.equals(a3));

		Assert.assertFalse(a1.equals(new Object()));
		Assert.assertFalse(a1 == null);
	}

}