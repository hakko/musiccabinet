package com.github.hakko.musiccabinet.service.lastfm;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcMusicFileDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.TrackRelationService;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.ws.lastfm.TrackSimilarityClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class TrackRelationServiceTest {
	
	@Autowired
	private TrackRelationService trackRelationService;

	@Autowired
	private JdbcMusicFileDao musicFileDao;
	
	private static final String CHER_SIMILAR_TRACKS = 
		"last.fm/xml/similartracks.cher.believe.xml";
	
	@Test
	public void trackRelationServiceConfigured() {
		Assert.assertNotNull(trackRelationService);
		Assert.assertNotNull(trackRelationService.trackSimilarityClient);
		Assert.assertNotNull(trackRelationService.trackRelationDao);
		Assert.assertNotNull(trackRelationService.musicFileDao);
	}
	
	@Test
	public void trackRelationInvokation() throws ApplicationException {

		PostgreSQLUtil.truncateTables(musicFileDao);
		
		WSResponse wsResponse = mock(WSResponse.class);
		when(wsResponse.wasCallAllowed()).thenReturn(true);
		when(wsResponse.wasCallSuccessful()).thenReturn(true);
		when(wsResponse.getResponseBody()).thenReturn(
				new ResourceUtil(CHER_SIMILAR_TRACKS).getContent());
		
		TrackSimilarityClient tsClient = mock(TrackSimilarityClient.class);
		when(tsClient.getTrackSimilarity(Mockito.any(Track.class))).thenReturn(wsResponse);

		trackRelationService.setTrackSimilarityClient(tsClient);

		MusicFile believe = new MusicFile("Cher", "Believe", "/path/to/believe", 0L, 0L);
		
		musicFileDao.clearImport();
		musicFileDao.addMusicFiles(Arrays.asList(believe));
		musicFileDao.createMusicFiles();
		
		trackRelationService.updateTrackRelation(believe.getPath());
	}

}