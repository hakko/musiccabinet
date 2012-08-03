package com.github.hakko.musiccabinet.service.lastfm;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.library.Period;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.UserTopArtistsService;

/*
 * Doesn't actually do much testing, except invoking the service methods.
 * 
 * Detailed test are found in @JdbcUserTopArtistsDao
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class UserTopArtistsServiceTest {

	@Autowired
	private UserTopArtistsService service;
	
	private static final int OFFSET = 0;
	private static final int LIMIT = 50;
	
	@Test
	public void serviceConfigured() {
		Assert.assertNotNull(service);
		Assert.assertNotNull(service.userTopArtistsClient);
		Assert.assertNotNull(service.userTopArtistsDao);
		Assert.assertNotNull(service.webserviceHistoryService);
	}
	
	@Test
	public void canInvokeService() throws ApplicationException {
		List<ArtistRecommendation> topArtists = service.getUserTopArtists(
				new LastFmUser("unknown user"), Period.OVERALL, OFFSET, LIMIT);
		
		Assert.assertNotNull(topArtists);
		Assert.assertEquals(0, topArtists.size());
	}

}