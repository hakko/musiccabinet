package com.github.hakko.musiccabinet.domain.model.music;

import org.junit.Test;

import junit.framework.Assert;

public class TrackRelationTest {

	@Test
	public void validateDefaultConstructor() {
		TrackRelation tr = new TrackRelation();
		
		Assert.assertNull(tr.getTarget());
	}
	
	@Test
	public void validateConstructor() {
		Track jump = new Track("madonna", "jump");
		float match = 0.22f;
		TrackRelation tr = new TrackRelation(jump, match);
		
		Assert.assertEquals(tr.getTarget(), jump);
		Assert.assertEquals(tr.getMatch(), match);
	}

	@Test
	public void validateSetters() {
		TrackRelation tr = new TrackRelation();
		
		Track believe = new Track("cher", "believe");
		float match = 0.33f;
		
		Assert.assertNotSame(tr.getTarget(), believe);
		Assert.assertFalse(tr.getMatch() == match);
		
		tr.setTarget(believe);
		Assert.assertEquals(tr.getTarget(), believe);

		tr.setMatch(match);
		Assert.assertEquals(tr.getMatch(), match);
	}

	@Test
	public void validateTrackRelationEquality() {
		TrackRelation tr1 = new TrackRelation(new Track("madonna", "jump"), 0.67f);
		TrackRelation tr2 = new TrackRelation(new Track("madonna", "jump"), 0.67f);
		TrackRelation tr3 = new TrackRelation(new Track("cher", "believe"), 0.67f);
		TrackRelation tr4 = new TrackRelation(new Track("madonna", "jump"), 0.77f);
		
		Assert.assertTrue(tr1.equals(tr1));
		Assert.assertTrue(tr1.equals(tr2));

		Assert.assertFalse(tr1.equals(tr3));
		Assert.assertFalse(tr1.equals(tr4));

		Assert.assertFalse(tr1.equals(new Object()));
		Assert.assertFalse(tr1 == null);
	}

}