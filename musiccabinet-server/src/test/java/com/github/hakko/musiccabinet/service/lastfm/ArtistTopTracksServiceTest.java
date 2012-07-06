package com.github.hakko.musiccabinet.service.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.ARTIST_GET_TOP_TRACKS;
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
import com.github.hakko.musiccabinet.dao.jdbc.JdbcWebserviceHistoryDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.ArtistTopTracksService;
import com.github.hakko.musiccabinet.service.lastfm.ThrottleService;
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
	private JdbcWebserviceHistoryDao webserviceHistoryDao;
	
	private static final String CHER_TOP_TRACKS = "last.fm/xml/toptracks.cher.xml";
	private static final String artistName = "cher";

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

		WebserviceInvocation wi = new WebserviceInvocation(ARTIST_GET_TOP_TRACKS, new Artist(artistName));
		Assert.assertTrue(webserviceHistoryDao.isWebserviceInvocationAllowed(wi));

		List<Artist> artists = webserviceHistoryDao.getArtistsScheduledForUpdate(ARTIST_GET_TOP_TRACKS);
		Assert.assertNotNull(artists);
		Assert.assertEquals(1, artists.size());
		Assert.assertTrue(artists.contains(new Artist(artistName)));

		ArtistTopTracksService artistTopTracksService = new ArtistTopTracksService();
		artistTopTracksService.setArtistTopTracksClient(getArtistTopTracksClient(webserviceHistoryDao));
		artistTopTracksService.setArtistTopTracksDao(artistTopTracksDao);
		artistTopTracksService.setWebserviceHistoryDao(webserviceHistoryDao);
		artistTopTracksService.updateSearchIndex();
		
		Assert.assertFalse(webserviceHistoryDao.isWebserviceInvocationAllowed(wi));
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
	private ArtistTopTracksClient getArtistTopTracksClient(WebserviceHistoryDao historyDao) throws IOException {
		// create a HTTP client that always returns Cher top tracks
		HttpClient httpClient = mock(HttpClient.class);
		ClientConnectionManager connectionManager = mock(ClientConnectionManager.class);
		when(httpClient.getConnectionManager()).thenReturn(connectionManager);
		String httpResponse = new ResourceUtil(CHER_TOP_TRACKS).getContent();
		when(httpClient.execute(Mockito.any(HttpUriRequest.class), 
				Mockito.any(ResponseHandler.class))).thenReturn(httpResponse);
		
		// create a throttling service that allows calls at any rate
		ThrottleService throttleService = mock(ThrottleService.class);

		// create a client that allows all calls and returns Cher top tracks
		ArtistTopTracksClient attClient = new ArtistTopTracksClient();
		attClient.setWebserviceHistoryDao(historyDao);
		attClient.setHttpClient(httpClient);
		attClient.setThrottleService(throttleService);

		return attClient;
	}

}