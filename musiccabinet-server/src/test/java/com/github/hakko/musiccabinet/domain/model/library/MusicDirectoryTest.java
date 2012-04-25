package com.github.hakko.musiccabinet.domain.model.library;

import junit.framework.Assert;

import org.junit.Test;

public class MusicDirectoryTest {

	@Test
	public void validateConstructor() {
		String artistName = "madonna";
		String path = "/path/";
		
		MusicDirectory md = new MusicDirectory(artistName, path);
		
		Assert.assertNotNull(md.getArtistName());
		Assert.assertNotNull(md.getPath());
		
		Assert.assertEquals(path, md.getPath());
		Assert.assertEquals(artistName, md.getArtistName());
	}

	@Test
	public void validateEquality() {
		String artist1 = "madonna";
		String path1 = "/path/";
		
		String artist2 = "cher";
		String path2 = "/other/";

		MusicDirectory md1 = new MusicDirectory(artist1, path1);
		MusicDirectory md2a = new MusicDirectory(artist2, path2);
		MusicDirectory md2b = new MusicDirectory(artist2, path2);
		
		Assert.assertFalse(md1 == null);
		Assert.assertFalse(md1.equals(new Object()));
		
		Assert.assertFalse(md1.equals(md2a));
		Assert.assertFalse(md1.equals(md2b));
		Assert.assertTrue(md2a.equals(md2b));
	}
	
}