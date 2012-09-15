package com.github.hakko.musiccabinet.service.lastfm;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.domain.model.aggr.GroupWeeklyArtistChart;
import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;
import com.github.hakko.musiccabinet.exception.ApplicationException;

/*
 * Doesn't actually do much testing, except invoking the service methods.
 * 
 * Detailed test are found in @JdbcGroupWeeklyArtistChartDaoTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class GroupWeeklyArtistChartServiceTest {

	@Autowired
	private GroupWeeklyArtistChartService service;
	
	@Test
	public void serviceConfigured() {
		Assert.assertNotNull(service);
		Assert.assertNotNull(service.client);
		Assert.assertNotNull(service.dao);
		Assert.assertNotNull(service.webserviceHistoryService);
		Assert.assertNotNull(service.lastFmDao);
	}
	
	@Test
	public void canInvokeService() throws ApplicationException {
		GroupWeeklyArtistChart artistChart = service.getWeeklyArtistChart(
				new LastFmGroup("group name"));
		
		Assert.assertNotNull(artistChart);
		Assert.assertEquals(0, artistChart.getArtistPlayCounts().size());
	}

}