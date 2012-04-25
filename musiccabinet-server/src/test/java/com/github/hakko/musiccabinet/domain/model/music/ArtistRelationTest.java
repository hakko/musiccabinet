package com.github.hakko.musiccabinet.domain.model.music;

import org.junit.Test;

import junit.framework.Assert;

public class ArtistRelationTest {

	@Test
	public void validateDefaultConstructor() {
		ArtistRelation ar = new ArtistRelation();
		
		Assert.assertNull(ar.getTarget());
	}
	
	@Test
	public void validateConstructor() {
		Artist madonna = new Artist("madonna");
		float match = 0.22f;
		ArtistRelation ar = new ArtistRelation(madonna, match);
		
		Assert.assertEquals(ar.getTarget(), madonna);
		Assert.assertEquals(ar.getMatch(), match);
	}

	@Test
	public void validateSetters() {
		ArtistRelation ar = new ArtistRelation();
		
		Artist cher = new Artist("cher");
		float match = 0.33f;
		
		Assert.assertNotSame(ar.getTarget(), cher);
		Assert.assertFalse(ar.getMatch() == match);
		
		ar.setTarget(cher);
		Assert.assertEquals(ar.getTarget(), cher);

		ar.setMatch(match);
		Assert.assertEquals(ar.getMatch(), match);
	}

	@Test
	public void validateArtisarelationEquality() {
		ArtistRelation ar1 = new ArtistRelation(new Artist("madonna"), 0.77f);
		ArtistRelation ar2 = new ArtistRelation(new Artist("madonna"), 0.77f);
		ArtistRelation ar3 = new ArtistRelation(new Artist("cher"), 0.77f);
		ArtistRelation ar4 = new ArtistRelation(new Artist("madonna"), 0.88f);
		
		Assert.assertTrue(ar1.equals(ar1));
		Assert.assertTrue(ar1.equals(ar2));

		Assert.assertFalse(ar1.equals(ar3));
		Assert.assertFalse(ar1.equals(ar4));

		Assert.assertFalse(ar1.equals(new Object()));
		Assert.assertFalse(ar1 == null);
	}

}