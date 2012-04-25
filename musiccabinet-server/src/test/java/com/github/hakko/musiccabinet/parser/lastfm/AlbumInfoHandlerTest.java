package com.github.hakko.musiccabinet.parser.lastfm;

import junit.framework.Assert;

import org.junit.Test;

public class AlbumInfoHandlerTest {

	private AlbumInfoHandler aih = new AlbumInfoHandler();
	
	private static final String EMPTY_URL = "";
	private static final String ERRONEUOS_URL = 
			"http://images.amazon.com/images/P/B00042YBXG.01.MZZZZZZZ.jpg";
	private static final String CORRECT_URL = 
			"http://userserve-ak.last.fm/serve/34s/72903330.png";

	@Test
	public void nullUrlReturnsNull() {
		Assert.assertNull(aih.validateUrl(null));
	}

	@Test
	public void emptyUrlReturnsNull() {
		Assert.assertNull(aih.validateUrl(EMPTY_URL));
	}

	@Test
	public void erroneousUrlReturnsNull() {
		Assert.assertNull(aih.validateUrl(ERRONEUOS_URL));
	}

	@Test
	public void correctUrlReturnsItself() {
		Assert.assertEquals(CORRECT_URL, aih.validateUrl(CORRECT_URL));
	}
	
}