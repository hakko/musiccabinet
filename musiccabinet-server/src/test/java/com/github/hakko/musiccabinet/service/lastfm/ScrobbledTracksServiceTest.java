package com.github.hakko.musiccabinet.service.lastfm;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

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

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.ScrobbledTracksService;
import com.github.hakko.musiccabinet.service.lastfm.ThrottleService;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.ws.lastfm.ScrobbledTracksClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class ScrobbledTracksServiceTest {

	@Autowired
	private ScrobbledTracksService scrobbledTracksService;

	private static final String SCROBBLED_TRACKS = "last.fm/xml/scrobbledtracks.xml";
	
	@Test
	public void scrobbledTracksServiceConfigured() throws ApplicationException {
		Assert.assertNotNull(scrobbledTracksService);
		Assert.assertNotNull(scrobbledTracksService.client);
		Assert.assertNotNull(scrobbledTracksService.trackPlayCountDao);
		Assert.assertNotNull(scrobbledTracksService.lastFmSettingsService);
	}
	
	@Test
	public void updatesScrobbledTracks() throws IOException, ApplicationException {
		ScrobbledTracksClient scrobbledTracksClient = getScrobbledTracksClient();
		scrobbledTracksService.setScrobbledTracksClient(scrobbledTracksClient);
		scrobbledTracksService.updateSearchIndex();
	}

	@SuppressWarnings("unchecked")
	private ScrobbledTracksClient getScrobbledTracksClient() throws IOException {
		// create a HistoryDao that allows all calls
		WebserviceHistoryService historyService = mock(WebserviceHistoryService.class);
		when(historyService.isWebserviceInvocationAllowed(
				Mockito.any(WebserviceInvocation.class))).thenReturn(true);

		// create a HTTP client that always returns sampled scrobbled tracks from last.fm
		HttpClient httpClient = mock(HttpClient.class);
		ClientConnectionManager connectionManager = mock(ClientConnectionManager.class);
		when(httpClient.getConnectionManager()).thenReturn(connectionManager);
		String httpResponse = new ResourceUtil(SCROBBLED_TRACKS).getContent();
		when(httpClient.execute(Mockito.any(HttpUriRequest.class), 
				Mockito.any(ResponseHandler.class))).thenReturn(httpResponse);

		// create a client out of the components above
		ScrobbledTracksClient stClient = new ScrobbledTracksClient();
		stClient.setWebserviceHistoryService(historyService);
		stClient.setHttpClient(httpClient);

		// create a throttling service that allows calls at any rate
		ThrottleService throttleService = mock(ThrottleService.class);
		stClient.setThrottleService(throttleService);

		return stClient;
	}

}