package com.github.hakko.musiccabinet.service;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class ItunesMusicLibraryServiceTest {

	@Autowired
	private ItunesMusicLibraryService itunesMusicLibraryService;

	private static final String ITUNES_INDEX_FILE = 
		"iTunes/iTunes Music Library.xml";
	
	@Test
	public void libraryIndexUpdateServiceConfigured() throws ApplicationException {
		Assert.assertNotNull(itunesMusicLibraryService);
		Assert.assertNotNull(itunesMusicLibraryService.musicFileDao);
		itunesMusicLibraryService.updateLibraryIndex(
				new ResourceUtil(ITUNES_INDEX_FILE).getInputStream());
	}
	
}