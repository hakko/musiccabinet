package com.github.hakko.musiccabinet.service.lastfm;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*
 * Doesn't actually do much testing, except control service configuration.
 * 
 * Detailed tests are found in @JdbcTagInfoDaoTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class TagInfoServiceTest {

	@Autowired
	private TagInfoService tagInfoService;
	
	@Test
	public void serviceConfigured() {
		Assert.assertNotNull(tagInfoService);
		Assert.assertNotNull(tagInfoService.tagInfoClient);
		Assert.assertNotNull(tagInfoService.tagInfoDao);
		Assert.assertNotNull(tagInfoService.tagDao);
		Assert.assertNotNull(tagInfoService.lastFmSettingsService);
	}

}