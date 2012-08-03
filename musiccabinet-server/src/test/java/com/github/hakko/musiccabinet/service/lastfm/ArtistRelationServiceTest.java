package com.github.hakko.musiccabinet.service.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.ARTIST_GET_SIMILAR;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Set;

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

import com.github.hakko.musiccabinet.dao.LibraryAdditionDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcArtistRelationDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;
import com.github.hakko.musiccabinet.ws.lastfm.ArtistSimilarityClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class ArtistRelationServiceTest {
	
	@Autowired
	private JdbcArtistRelationDao artistRelationDao;
	
	@Autowired
	private ArtistRelationService artistRelationService;

	@Autowired
	private WebserviceHistoryService webserviceHistoryService;

	@Autowired
	private LibraryAdditionDao additionDao;
	
	private static final String CHER_ARTIST_RELATIONS = "last.fm/xml/similarartists.cher.xml";
	private static final String artistName = "cher";
	
	@Test
	public void artistRelationServiceConfigured() {
		Assert.assertNotNull(artistRelationService);
		Assert.assertNotNull(artistRelationService.artistRelationDao);
		Assert.assertNotNull(artistRelationService.artistSimilarityClient);
		Assert.assertNotNull(artistRelationService.webserviceHistoryService);
	}
	
	@Test
	public void artistRelationUpdateUpdatesAllArtists() throws ApplicationException, IOException {
		clearLibraryAndAddCherTrack();

		WebserviceInvocation wi = new WebserviceInvocation(ARTIST_GET_SIMILAR, new Artist(artistName));
		Assert.assertTrue(webserviceHistoryService.isWebserviceInvocationAllowed(wi));

		Set<String> artistNames = webserviceHistoryService.getArtistNamesScheduledForUpdate(ARTIST_GET_SIMILAR);
		Assert.assertNotNull(artistNames);
		Assert.assertEquals(1, artistNames.size());
		Assert.assertTrue(artistNames.contains(artistName));

		ArtistRelationService artistRelationService = new ArtistRelationService();
		artistRelationService.setArtistSimilarityClient(getArtistSimilarityClient(webserviceHistoryService));
		artistRelationService.setArtistRelationDao(artistRelationDao);
		artistRelationService.setWebserviceHistoryService(webserviceHistoryService);
		artistRelationService.updateSearchIndex();
		
		Assert.assertFalse(webserviceHistoryService.isWebserviceInvocationAllowed(wi));
	}
	
	private void clearLibraryAndAddCherTrack() throws ApplicationException {
		PostgreSQLUtil.truncateTables(artistRelationDao);
		
		File file = getFile(artistName, null, "Believe");
		UnittestLibraryUtil.submitFile(additionDao, file);
	}
	
	@SuppressWarnings("unchecked")
	private ArtistSimilarityClient getArtistSimilarityClient(WebserviceHistoryService historyService) throws IOException {
		// create a HTTP client that always returns Cher artist relations
		HttpClient httpClient = mock(HttpClient.class);
		ClientConnectionManager connectionManager = mock(ClientConnectionManager.class);
		when(httpClient.getConnectionManager()).thenReturn(connectionManager);
		String httpResponse = new ResourceUtil(CHER_ARTIST_RELATIONS).getContent();
		when(httpClient.execute(Mockito.any(HttpUriRequest.class), 
				Mockito.any(ResponseHandler.class))).thenReturn(httpResponse);
		
		// create a throttling service that allows calls at any rate
		ThrottleService throttleService = mock(ThrottleService.class);

		// create a client that allows all calls and returns Cher artist relations
		ArtistSimilarityClient asClient = new ArtistSimilarityClient();
		asClient.setWebserviceHistoryService(historyService);
		asClient.setHttpClient(httpClient);
		asClient.setThrottleService(throttleService);

		return asClient;
	}

}