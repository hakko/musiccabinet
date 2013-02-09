package com.github.hakko.musiccabinet.service.lastfm;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*
 * Doesn't actually do much testing, except invoking the service methods.
 * 
 * Detailed test are found in @JdbcUserRecommendedArtistsDaoTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class UserRecommendedArtistsServiceTest {

	@Autowired
	private UserRecommendedArtistsService service;

	@Test
	public void serviceConfigured() {
		Assert.assertNotNull(service);
		Assert.assertNotNull(service.userRecommendedArtistsClient);
		Assert.assertNotNull(service.dao);
		Assert.assertNotNull(service.webserviceHistoryService);
		Assert.assertNotNull(service.lastFmSettingsService);
	}

}