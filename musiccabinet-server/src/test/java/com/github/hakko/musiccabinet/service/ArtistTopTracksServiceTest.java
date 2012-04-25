package com.github.hakko.musiccabinet.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.MusicFileDao;
import com.github.hakko.musiccabinet.dao.WebserviceHistoryDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcArtistTopTracksDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.ws.lastfm.ArtistTopTracksClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class ArtistTopTracksServiceTest {

	@Autowired
	private MusicFileDao musicFileDao;
	
	@Autowired
	private JdbcArtistTopTracksDao artistTopTracksDao;
	
	@Autowired
	private ArtistTopTracksService artistTopTracksService;

	@Autowired
	private WebserviceHistoryDao webserviceHistoryDao;
	
	private static final String CHER_TOP_TRACKS = "last.fm/xml/toptracks.cher.xml";
	private static final String artistName = "cher";
	
	@Test
	public void testdataFoundOnClasspath() {
		new ResourceUtil(CHER_TOP_TRACKS);
	}

	@Test
	public void artistTopTracksServiceConfigured() {
		Assert.assertNotNull(artistTopTracksService);
		Assert.assertNotNull(artistTopTracksService.artistTopTracksDao);
		Assert.assertNotNull(artistTopTracksService.artistTopTracksClient);
		Assert.assertNotNull(artistTopTracksService.webserviceHistoryDao);
	}
	
	@Test
	public void artistTopTracksUpdateUpdatesAllArtists() throws ApplicationException, IOException {
		clearLibraryAndAddCherTrack();
		
		List<Artist> artists = artistTopTracksDao.getArtistsWithoutTopTracks();
		Assert.assertNotNull(artists);
		Assert.assertEquals(1, artists.size());
		Assert.assertTrue(artists.contains(new Artist(artistName)));

		ArtistTopTracksService artistTopTracksService = new ArtistTopTracksService();
		artistTopTracksService.setArtistTopTracksClient(getArtistTopTracksClient());
		artistTopTracksService.setArtistTopTracksDao(artistTopTracksDao);
		artistTopTracksService.setWebserviceHistoryDao(webserviceHistoryDao);
		artistTopTracksService.updateSearchIndex();
		
		artists = artistTopTracksDao.getArtistsWithoutTopTracks();
		Assert.assertNotNull(artists);
		Assert.assertEquals(0, artists.size());
	}
	
	private void clearLibraryAndAddCherTrack() throws ApplicationException {
		PostgreSQLUtil.truncateTables(artistTopTracksDao);

		long time = System.currentTimeMillis();
		MusicFile mf = new MusicFile(artistName, "Believe", "/", time, time);

		musicFileDao.clearImport();
		musicFileDao.addMusicFiles(Arrays.asList(mf));
		musicFileDao.createMusicFiles();
	}
	
	@SuppressWarnings("unchecked")
	private ArtistTopTracksClient getArtistTopTracksClient() throws IOException {
		// create a HistoryDao that allow all calls
		WebserviceHistoryDao historyDao = mock(WebserviceHistoryDao.class);
		when(historyDao.isWebserviceInvocationAllowed(
				Mockito.any(WebserviceInvocation.class))).thenReturn(true);

		// create a HTTP client that always returns Cher top tracks
		HttpClient httpClient = mock(HttpClient.class);
		ClientConnectionManager connectionManager = mock(ClientConnectionManager.class);
		when(httpClient.getConnectionManager()).thenReturn(connectionManager);
		String httpResponse = new ResourceUtil(CHER_TOP_TRACKS).getContent();
		when(httpClient.execute(Mockito.any(HttpUriRequest.class), 
				Mockito.any(ResponseHandler.class))).thenReturn(httpResponse);

		// create a client that allows all calls and returns Cher top tracks
		ArtistTopTracksClient attClient = new ArtistTopTracksClient();
		attClient.setWebserviceHistoryDao(historyDao);
		attClient.setHttpClient(httpClient);
		
		// create a throttling service that allows calls at any rate
		ThrottleService throttleService = mock(ThrottleService.class);
		attClient.setThrottleService(throttleService);

		return attClient;
	}

}