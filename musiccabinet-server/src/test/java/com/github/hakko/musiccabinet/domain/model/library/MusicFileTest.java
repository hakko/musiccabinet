package com.github.hakko.musiccabinet.domain.model.library;

import junit.framework.Assert;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.Track;

public class MusicFileTest {

	@Test
	public void validateConstructor() {
		String artistName = "madonna";
		String trackName = "like a virgin";
		String path = "/path/";
		long lastModified = System.currentTimeMillis();
		long created = System.currentTimeMillis() - 1000;
		
		MusicFile mf = new MusicFile(artistName, trackName, path, created, lastModified);

		Assert.assertNotNull(mf.getLastModified());
		Assert.assertNotNull(mf.getCreated());
		
		Assert.assertEquals(mf.getTrack(), new Track(artistName, trackName));
		Assert.assertEquals(mf.getPath(), path);
		Assert.assertEquals(mf.getLastModified().getMillis(), lastModified);
		Assert.assertEquals(mf.getCreated().getMillis(), created);
	}

	@Test
	public void validateEquality() {
		String artist1 = "madonna";
		String track1 = "like a virgin";
		String path1 = "/path/";
		long modified1 = System.currentTimeMillis();
		long created1 = System.currentTimeMillis();
		
		String artist2 = "cher";
		String track2 = "strong enough";
		String path2 = "/other/";
		long modified2 = System.currentTimeMillis() + 1000;
		long created2 = System.currentTimeMillis() + 1000;

		MusicFile mf1 = new MusicFile(artist1, track1, path1, created1, modified1);
		MusicFile mf2 = new MusicFile(artist1, track1, path1, created1, modified1);

		MusicFile mf3 = new MusicFile(artist2, track1, path1, created1, modified1);
		MusicFile mf4 = new MusicFile(artist1, track2, path1, created1, modified1);
		MusicFile mf5 = new MusicFile(artist1, track1, path2, created1, modified1);
		MusicFile mf6 = new MusicFile(artist1, track1, path1, created1, modified2);
		MusicFile mf7 = new MusicFile(artist1, track1, path1, created2, modified1);

		Assert.assertTrue(mf1.equals(mf1));
		Assert.assertTrue(mf1.equals(mf2));
		
		Assert.assertFalse(mf1.equals(mf3));
		Assert.assertFalse(mf1.equals(mf4));
		Assert.assertFalse(mf1.equals(mf5));
		Assert.assertFalse(mf1.equals(mf6));
		Assert.assertFalse(mf1.equals(mf7));

		Assert.assertFalse(mf1.equals(new Object()));
		Assert.assertFalse(mf1 == null);
	}
	
}