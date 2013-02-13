package com.github.hakko.musiccabinet.service.lastfm;

import static junit.framework.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*
 * Doesn't actually do much testing, except checking injected dependencies.
 * 
 * Detailed tests are found in @JdbcUserLovedTracksDaoTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class UserLovedTracksServiceTest {

	@Autowired
	private UserLovedTracksService service;

	@Test
	public void serviceConfigured() {
		assertNotNull(service);
		assertNotNull(service.webserviceHistoryService);
		assertNotNull(service.lastFmSettingsService);
		assertNotNull(service.userLovedTracksClient);
		assertNotNull(service.userLovedTracksDao);
		assertNotNull(service.trackLoveClient);
		assertNotNull(service.starService);
	}

}