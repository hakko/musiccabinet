package com.github.hakko.musiccabinet.parser.subsonic;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.library.MusicDirectory;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class SubsonicIndexParserTest {
	
	private static final String LIBRARY_INDEX_FILE = 
		"subsonic/subsonic15.index";

	@SuppressWarnings("unchecked")
	@Test (expected = ApplicationException.class)
	public void corruptStreamThrowsException() throws IOException, ApplicationException {
		InputStream faultyInputStream = mock(InputStream.class);
		when(faultyInputStream.read()).thenThrow(IOException.class);
		new SubsonicIndexParserImpl(faultyInputStream).readBatch();
	}
	
	@Test
	public void parseAndValidateMusicFiles() throws ApplicationException {
		
		SubsonicIndexParser parser = new SubsonicIndexParserImpl(
				new ResourceUtil(LIBRARY_INDEX_FILE).getInputStream());
		
		List<MusicFile> musicFiles = null;
		while (parser.readBatch());
		musicFiles = parser.getMusicFiles();

		MusicFile last = musicFiles.get(musicFiles.size() - 1);
		assertNotNull(last);
		assertNotNull(last.getTrack());
		assertNotNull(last.getTrack().getArtist());

		assertEquals(last.getTrack().getArtist().getName(), "ÓLAFUR ARNALDS");
		assertEquals(last.getTrack().getName(), "HIMININN ER AÐ HRYNJA, EN STJÖRNURNAR FARA ÞÉR VEL");
		assertEquals(last.getPath(), "/Users/hakko/Music/Library/Ólafur Arnalds/Variations Of Static/05 Himininn er að hrynja, en stjörnu.mp3");
		assertEquals(1293227206000L, last.getCreated().getMillis());
		assertEquals(1293227206000L, last.getLastModified().getMillis());
		
	}

	@Test
	public void parseAndValidateMusicDirectories() throws ApplicationException {
		SubsonicIndexParser parser = new SubsonicIndexParserImpl(
				new ResourceUtil(LIBRARY_INDEX_FILE).getInputStream());
		
		List<MusicDirectory> musicDirectories = null;
		parser.readBatch();
		musicDirectories = parser.getMusicDirectories();

		Assert.assertNotNull(musicDirectories);
		Assert.assertTrue(musicDirectories.size() > 0);

		MusicDirectory md0 = musicDirectories.get(0);
		Assert.assertNotNull(md0);
		Assert.assertEquals("Big K.R.I.T.", md0.getArtistName());
		Assert.assertEquals("/Users/hakko/Music/Library/Big K.R.I.T_", 
				md0.getPath());
		Assert.assertTrue(md0.isRoot());

		MusicDirectory md1 = musicDirectories.get(1);
		Assert.assertNotNull(md1);
		Assert.assertEquals("BIG K.R.I.T.", md1.getArtistName());
		Assert.assertEquals("/Users/hakko/Music/Library/Big K.R.I.T_/ReturnOf4Eva", 
				md1.getPath());
		Assert.assertFalse(md1.isRoot());

		MusicDirectory md2 = musicDirectories.get(2);
		Assert.assertNotNull(md2);
		Assert.assertEquals("Compilations", md2.getArtistName());
		Assert.assertEquals("/Users/hakko/Music/Library/Compilations", 
				md2.getPath());
		Assert.assertTrue(md2.isRoot());

		MusicDirectory md3 = musicDirectories.get(3);
		Assert.assertNotNull(md3);
		Assert.assertEquals("Tim Roth & Amanda Plummer / Dick Dale & His Del-Tones", md3.getArtistName());
		Assert.assertEquals("OST - Pulp Fiction", md3.getAlbumName());
		Assert.assertEquals("/Users/hakko/Music/Library/Compilations/OST - Pulp Fiction", 
				md3.getPath());
		Assert.assertFalse(md3.isRoot());

		MusicDirectory md4 = musicDirectories.get(4);
		Assert.assertNotNull(md4);
		Assert.assertEquals("God Is an Astronaut", md4.getArtistName());
		Assert.assertEquals("/Users/hakko/Music/Library/God Is an Astronaut", 
				md4.getPath());
		Assert.assertTrue(md4.isRoot());

		MusicDirectory md5 = musicDirectories.get(5);
		Assert.assertNotNull(md5);
		Assert.assertEquals("GOD IS AN ASTRONAUT", md5.getArtistName());
		Assert.assertEquals("/Users/hakko/Music/Library/God Is an Astronaut/All Is Violent, All Is Bright", 
				md5.getPath());
		Assert.assertFalse(md5.isRoot());

		MusicDirectory md6 = musicDirectories.get(6);
		Assert.assertNotNull(md6);
		Assert.assertEquals("GOD IS AN ASTRONAUT", md6.getArtistName());
		Assert.assertEquals("/Users/hakko/Music/Library/God Is an Astronaut/The End Of The Beginning", 
				md6.getPath());
		Assert.assertFalse(md6.isRoot());
	}
	
}