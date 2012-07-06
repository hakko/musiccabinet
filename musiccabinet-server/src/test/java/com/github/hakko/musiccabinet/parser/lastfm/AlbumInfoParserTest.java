package com.github.hakko.musiccabinet.parser.lastfm;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.music.AlbumInfo;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class AlbumInfoParserTest {
	
	private static final String ALBUM_INFO_FILE = 
		"last.fm/xml/albuminfo.nirvana.nevermind.xml";

	// constant values below are copied from file above
	
	private static final String SMALL_IMAGE_URL = 
			"http://userserve-ak.last.fm/serve/34s/63911473.png";
	private static final String MEDIUM_IMAGE_URL = 
			"http://userserve-ak.last.fm/serve/64s/63911473.png";
	private static final String LARGE_IMAGE_URL = 
			"http://userserve-ak.last.fm/serve/174s/63911473.png";
	private static final String EXTRA_LARGE_IMAGE_URL =
			"http://userserve-ak.last.fm/serve/300x300/63911473.png";
	
	private static final int LISTENERS = 1869409;
	private static final int PLAY_COUNT = 29066127;
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		AlbumInfoParser parser = new AlbumInfoParserImpl(
				new ResourceUtil(ALBUM_INFO_FILE).getInputStream());
		
		AlbumInfo albumInfo = parser.getAlbumInfo();
		
		assertEquals(new Artist("Nirvana"), albumInfo.getAlbum().getArtist());
		
		assertEquals(SMALL_IMAGE_URL, albumInfo.getSmallImageUrl());
		assertEquals(MEDIUM_IMAGE_URL, albumInfo.getMediumImageUrl());
		assertEquals(LARGE_IMAGE_URL, albumInfo.getLargeImageUrl());
		assertEquals(EXTRA_LARGE_IMAGE_URL, albumInfo.getExtraLargeImageUrl());
		
		assertEquals(LISTENERS, albumInfo.getListeners());
		assertEquals(PLAY_COUNT, albumInfo.getPlayCount());
	}

}