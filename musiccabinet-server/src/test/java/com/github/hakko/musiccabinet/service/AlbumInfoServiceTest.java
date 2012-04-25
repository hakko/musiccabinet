package com.github.hakko.musiccabinet.service;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.domain.model.music.AlbumInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;

/*
 * Doesn't actually do much testing, except invoking the service methods.
 * 
 * Detailed test are found in JdbcAlbumInfoDaoTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class AlbumInfoServiceTest {

	@Autowired
	private AlbumInfoService aiService;
	
	@Test
	public void serviceConfigured() {
		Assert.assertNotNull(aiService);
		Assert.assertNotNull(aiService.albumInfoDao);
		Assert.assertNotNull(aiService.albumInfoClient);
		Assert.assertNotNull(aiService.webserviceHistoryDao);
	}
	
	@Test
	public void canInvokeService() throws ApplicationException {
		List<AlbumInfo> albums = aiService.getAlbumInfosForArtist("UNDEFINED ARTIST");
		
		Assert.assertNotNull(albums);
		Assert.assertEquals(0, albums.size());
	}

}