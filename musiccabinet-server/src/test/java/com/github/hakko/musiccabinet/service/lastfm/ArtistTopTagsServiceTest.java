package com.github.hakko.musiccabinet.service.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.ARTIST_GET_TOP_TAGS;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import com.github.hakko.musiccabinet.dao.jdbc.JdbcArtistTopTagsDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;
import com.github.hakko.musiccabinet.ws.lastfm.ArtistTopTagsClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class ArtistTopTagsServiceTest {

	@Autowired
	private LibraryAdditionDao additionDao;
	
	@Autowired
	private JdbcArtistTopTagsDao artistTopTagsDao;
	
	@Autowired
	private ArtistTopTagsService artistTopTagsService;

	@Autowired
	private WebserviceHistoryService webserviceHistoryService;
	
	private static final String CHER_TOP_TAGS = "last.fm/xml/toptags.cher.xml";
	private static final String artistName = "cher";

	@Test
	public void artistTopTagsServiceConfigured() {
		Assert.assertNotNull(artistTopTagsService);
		Assert.assertNotNull(artistTopTagsService.artistTopTagsDao);
		Assert.assertNotNull(artistTopTagsService.webserviceHistoryService);
		Assert.assertNotNull(artistTopTagsService.artistTopTagsClient);
	}
	
	@Test
	public void artistTopTagsUpdateUpdatesAllArtists() throws ApplicationException, IOException {
		clearLibraryAndAddCherTrack();
		
		WebserviceInvocation wi = new WebserviceInvocation(ARTIST_GET_TOP_TAGS, new Artist(artistName));
		Assert.assertTrue(webserviceHistoryService.isWebserviceInvocationAllowed(wi));
		
		Set<String> artistNames = webserviceHistoryService.getArtistNamesScheduledForUpdate(ARTIST_GET_TOP_TAGS);
		Assert.assertNotNull(artistNames);
		Assert.assertEquals(1, artistNames.size());
		Assert.assertTrue(artistNames.contains(artistName));

		ArtistTopTagsService artistTopTagsService = new ArtistTopTagsService();
		artistTopTagsService.setArtistTopTagsClient(getArtistTopTagsClient(webserviceHistoryService));
		artistTopTagsService.setArtistTopTagsDao(artistTopTagsDao);
		artistTopTagsService.setWebserviceHistoryService(webserviceHistoryService);
		artistTopTagsService.updateSearchIndex();

		Assert.assertFalse(webserviceHistoryService.isWebserviceInvocationAllowed(wi));
	}
	
	@Test
	public void tagsWithLowTagCountGetStripped() {
		final String folk = "folk", psychedelic = "psychedelic", disco = "disco";
		
		List<Tag> tags = new ArrayList<>();
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

		File file = getFile(artistName, null, "Believe");
		UnittestLibraryUtil.submitFile(additionDao, file);
	}
	
	@SuppressWarnings("unchecked")
	private ArtistTopTagsClient getArtistTopTagsClient(WebserviceHistoryService historyService) throws IOException {
		// create a HTTP client that always returns Cher top tracks
		HttpClient httpClient = mock(HttpClient.class);
		ClientConnectionManager connectionManager = mock(ClientConnectionManager.class);
		when(httpClient.getConnectionManager()).thenReturn(connectionManager);
		String httpResponse = new ResourceUtil(CHER_TOP_TAGS).getContent();
		when(httpClient.execute(Mockito.any(HttpUriRequest.class), 
				Mockito.any(ResponseHandler.class))).thenReturn(httpResponse);

		// create a throttling service that allows calls at any rate
		ThrottleService throttleService = mock(ThrottleService.class);

		// create a client based on mocked HTTP client
		ArtistTopTagsClient attClient = new ArtistTopTagsClient();
		attClient.setWebserviceHistoryService(historyService);
		attClient.setHttpClient(httpClient);
		attClient.setThrottleService(throttleService);

		return attClient;
	}

}