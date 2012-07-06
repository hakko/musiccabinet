package com.github.hakko.musiccabinet.parser.itunes;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class ItunesMusicLibraryParserTest {
	
	private static final String ITUNES_INDEX_FILE = 
		"iTunes/iTunes Music Library.xml";
	
	@Test
	public void resourceFileCorrectlyParsed() throws ApplicationException {
		
		final AtomicInteger counter = new AtomicInteger(0);
		
		ItunesMusicLibraryParserCallback callback = new 
		ItunesMusicLibraryParserCallback() {
			
			@Override
			public void endOfMusicFiles() {
			}
			
			@Override
			public void addMusicFile(MusicFile musicFile) {
				counter.incrementAndGet();
			}
		};
		new ItunesMusicLibraryParserImpl(
				new ResourceUtil(ITUNES_INDEX_FILE).getInputStream(), callback);
	
		Assert.assertEquals(10, counter.intValue());
	}
	
}