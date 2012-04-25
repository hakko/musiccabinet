package com.github.hakko.musiccabinet.domain.model.library;

import junit.framework.Assert;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.Track;

public class TrackPlayCountTest {

	@Test
	public void validateDefaultConstructor() {
		TrackPlayCount tpc = new TrackPlayCount();
		
		Assert.assertNull(tpc.getTrack());
	}
	
	@Test
	public void validateConstructor() {
		String artistName = "madonna";
		String trackName = "material girl";
		int playCount = 30;
		
		TrackPlayCount tpc = new TrackPlayCount(artistName, trackName, playCount);
		
		Assert.assertEquals(tpc.getTrack(), new Track(artistName, trackName));
		Assert.assertEquals(tpc.getPlayCount(), playCount);
	}
	
	@Test
	public void validateSetters() {
		String artistName = "madonna";
		String trackName = "material girl";
		int playCount = 30;

		TrackPlayCount tpc = new TrackPlayCount();
		
		tpc.setTrack(new Track(artistName, trackName));
		tpc.setPlayCount(playCount);

		Assert.assertEquals(tpc.getTrack(), new Track(artistName, trackName));
		Assert.assertEquals(tpc.getPlayCount(), playCount);
	}
	
	@Test
	public void validateEquality() {
		String artist1 = "madonna";
		String track1 = "material girl";
		int playCount1 = 22;

		String artist2 = "cindy lauper";
		String track2 = "girls just want to have fun";
		int playCount2 = 24;
		
		TrackPlayCount tpc1 = new TrackPlayCount(artist1, track1, playCount1);
		TrackPlayCount tpc2 = new TrackPlayCount(artist1, track1, playCount1);

		TrackPlayCount tpc3 = new TrackPlayCount(artist2, track1, playCount1);
		TrackPlayCount tpc4 = new TrackPlayCount(artist1, track2, playCount1);
		TrackPlayCount tpc5 = new TrackPlayCount(artist1, track1, playCount2);
	
		Assert.assertTrue(tpc1.equals(tpc1));
		Assert.assertTrue(tpc1.equals(tpc2));

		Assert.assertFalse(tpc1.equals(tpc3));
		Assert.assertFalse(tpc1.equals(tpc4));
		Assert.assertFalse(tpc1.equals(tpc5));
		
		Assert.assertFalse(tpc1.equals(new Object()));
		Assert.assertFalse(tpc1 == null);
	}
	
}