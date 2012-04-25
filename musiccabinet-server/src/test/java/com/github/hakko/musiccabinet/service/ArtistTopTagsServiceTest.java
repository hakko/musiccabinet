package com.github.hakko.musiccabinet.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
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
import com.github.hakko.musiccabinet.dao.jdbc.JdbcArtistTopTagsDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.ws.lastfm.ArtistTopTagsClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class ArtistTopTagsServiceTest {

	@Autowired
	private MusicFileDao musicFileDao;
	
	@Autowired
	private JdbcArtistTopTagsDao artistTopTagsDao;
	
	@Autowired
	private ArtistTopTagsService artistTopTagsService;

	@Autowired
	private WebserviceHistoryDao webserviceHistoryDao;
	
	private static final String CHER_TOP_TAGS = "last.fm/xml/toptags.cher.xml";
	private static final String artistName = "cher";
	
	@Test
	public void testdataFoundOnClasspath() {
		new ResourceUtil(CHER_TOP_TAGS);
	}

	@Test
	public void artistTopTagsServiceConfigured() {
		Assert.assertNotNull(artistTopTagsService);
		Assert.assertNotNull(artistTopTagsService.artistTopTagsDao);
		Assert.assertNotNull(artistTopTagsService.webserviceHistoryDao);
		Assert.assertNotNull(artistTopTagsService.artistTopTagsClient);
	}
	
	@Test
	public void artistTopTagsUpdateUpdatesAllArtists() throws ApplicationException, IOException {
		clearLibraryAndAddCherTrack();
		
		List<Artist> artists = artistTopTagsDao.getArtistsWithoutTopTags();
		Assert.assertNotNull(artists);
		Assert.assertEquals(1, artists.size());
		Assert.assertTrue(artists.contains(new Artist(artistName)));

		ArtistTopTagsService artistTopTagsService = new ArtistTopTagsService();
		artistTopTagsService.setArtistTopTagsClient(getArtistTopTagsClient());
		artistTopTagsService.setArtistTopTagsDao(artistTopTagsDao);
		artistTopTagsService.setWebserviceHistoryDao(webserviceHistoryDao);
		artistTopTagsService.updateSearchIndex();
		
		artists = artistTopTagsDao.getArtistsWithoutTopTags();
		Assert.assertNotNull(artists);
		Assert.assertEquals(0, artists.size());
	}
	
	@Test
	public void tagsWithLowTagCountGetStripped() {
		final String folk = "folk", psychedelic = "psychedelic", disco = "disco";
		
		List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag(folk, (short) 99));
		tags.add(new Tag(psychedelic, (short) 84));
		tags.add(new Tag(disco, (short) 0));
		
		Assert.assertEquals(3, tags.size());

		artistTopTagsService.removeTagsWithLowTagCount(tags);

		Assert.assertEquals(2, tags.size());
		for (Tag tag : tags) {
			Assert.assertTrue(tag.getCount() > 0);
			Assert.assertFalse(tag.getName().equals(disco));
		}
	}
	
	private void clearLibraryAndAddCherTrack() throws ApplicationException {
		PostgreSQLUtil.truncateTables(artistTopTagsDao);

		long time = System.currentTimeMillis();
		MusicFile mf = new MusicFile(artistName, "Believe", "/", time, time);

		musicFileDao.clearImport();
		musicFileDao.addMusicFiles(Arrays.asList(mf));
		musicFileDao.createMusicFiles();
	}
	
	@SuppressWarnings("unchecked")
	private ArtistTopTagsClient getArtistTopTagsClient() throws IOException {
		// create a HistoryDao that allow all calls
		WebserviceHistoryDao historyDao = mock(WebserviceHistoryDao.class);
		when(historyDao.isWebserviceInvocationAllowed(
				Mockito.any(WebserviceInvocation.class))).thenReturn(true);

		// create a HTTP client that always returns Cher top tracks
		HttpClient httpClient = mock(HttpClient.class);
		ClientConnectionManager connectionManager = mock(ClientConnectionManager.class);
		when(httpClient.getConnectionManager()).thenReturn(connectionManager);
		String httpResponse = new ResourceUtil(CHER_TOP_TAGS).getContent();
		when(httpClient.execute(Mockito.any(HttpUriRequest.class), 
				Mockito.any(ResponseHandler.class))).thenReturn(httpResponse);

		// create a client that allows all calls and returns Cher top tracks
		ArtistTopTagsClient attClient = new ArtistTopTagsClient();
		attClient.setWebserviceHistoryDao(historyDao);
		attClient.setHttpClient(httpClient);

		// create a throttling service that allows calls at any rate
		ThrottleService throttleService = mock(ThrottleService.class);
		attClient.setThrottleService(throttleService);

		return attClient;
	}

}