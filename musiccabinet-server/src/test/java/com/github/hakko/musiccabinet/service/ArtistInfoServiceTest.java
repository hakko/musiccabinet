package com.github.hakko.musiccabinet.service;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;

/*
 * Doesn't actually do much testing, except invoking the service methods.
 * 
 * Detailed test are found in JdbcArtistInfoDaoTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class ArtistInfoServiceTest {

	@Autowired
	private ArtistInfoService aiService;
	
	@Test
	public void serviceConfigured() {
		Assert.assertNotNull(aiService);
		Assert.assertNotNull(aiService.artistInfoDao);
		Assert.assertNotNull(aiService.artistInfoClient);
		Assert.assertNotNull(aiService.musicDirectoryDao);
		Assert.assertNotNull(aiService.webserviceHistoryDao);
	}
	
	@Test
	public void canInvokeService() throws ApplicationException {
		ArtistInfo artistInfo = aiService.getArtistInfo("/path/leading/nowhere");
		
		Assert.assertNull(artistInfo);
	}

}