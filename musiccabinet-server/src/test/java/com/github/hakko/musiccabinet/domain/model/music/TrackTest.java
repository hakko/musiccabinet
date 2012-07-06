package com.github.hakko.musiccabinet.domain.model.music;

import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.Test;

public class TrackTest {

	@Test
	public void shouldCreateTrack() {
		String artistName = "Cher";
		Artist artist = new Artist(artistName);

		String trackName = "Believe";
		Track track = new Track(artist, trackName);
		
		assertTrue(trackName.equals(track.getName()));
		assertTrue(artist.equals(track.getArtist()));
	}
	
	@Test
	public void validateTrackEquality() {
		Track t1 = new Track("cher", "believe");
		Track t2 = new Track("cher", "believe");

		Track t3 = new Track("madonna", "jump");
		
		Assert.assertTrue(t1.equals(t1));
		Assert.assertTrue(t1.equals(t2));
		Assert.assertTrue(t2.equals(t2));
		
		Assert.assertFalse(t1.equals(t3));
		Assert.assertFalse(t2.equals(t3));
		
		Assert.assertFalse(t1.equals(new Object()));
		Assert.assertFalse(t1 == null);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void trackNameIsNullsafe() {
		new Track("madonna", (String) null);
	}

	@Test (expected = IllegalArgumentException.class)
	public void trackArtistIsNullsafe() {
		new Track((Artist) null, "jump");
	}

}