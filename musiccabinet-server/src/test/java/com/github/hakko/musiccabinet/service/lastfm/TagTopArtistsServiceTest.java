package com.github.hakko.musiccabinet.service.lastfm;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class TagTopArtistsServiceTest {

	@Autowired
	private TagTopArtistsService service;
	
	@Test
	public void serviceConfigured() {
		Assert.assertNotNull(service);
		Assert.assertNotNull(service.tagTopArtistsClient);
		Assert.assertNotNull(service.tagDao);
		Assert.assertNotNull(service.webserviceHistoryService);
	}

}