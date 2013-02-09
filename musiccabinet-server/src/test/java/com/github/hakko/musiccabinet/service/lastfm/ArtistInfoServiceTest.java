package com.github.hakko.musiccabinet.service.lastfm;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.ArtistInfoService;

/*
 * Doesn't actually do much testing, except invoking the service methods.
 * 
 * Detailed tests are found in JdbcArtistInfoDaoTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class ArtistInfoServiceTest {

	@Autowired
	private ArtistInfoService aiService;

	@Autowired
	private MusicDao musicDao;
	
	@Test
	public void serviceConfigured() {
		Assert.assertNotNull(aiService);
		Assert.assertNotNull(aiService.artistInfoDao);
		Assert.assertNotNull(aiService.artistInfoClient);
		Assert.assertNotNull(aiService.webserviceHistoryService);
		Assert.assertNotNull(aiService.lastFmSettingsService);
	}
	
	@Test
	public void canInvokeService() throws ApplicationException {
		String artistName = "A Previously Unknown Artist";
		int artistId = musicDao.getArtistId(artistName);
		
		ArtistInfo artistInfo = aiService.getArtistInfo(artistId);
		
		Assert.assertNotNull(artistInfo);
		Assert.assertEquals(artistName, artistInfo.getArtist().getName());
	}

}