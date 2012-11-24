package com.github.hakko.musiccabinet.ws.musicbrainz;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

public class ArtistQueryClientTest {
	
	@Test
	@SuppressWarnings("unchecked")
	public void invokesHttpRequestToExpectedUri() throws Exception {
		ArtistQueryClient artistQueryClient = getArtistQueryClient();

		final String ARTIST_NAME = "Nirvana", EXPECTED_URI =
				"http://musicbrainz.org/ws/2/artist/?query=Nirvana&limit=1";

		ArgumentCaptor<HttpGet> argument = forClass(HttpGet.class);
		artistQueryClient.get(ARTIST_NAME);
		verify(artistQueryClient.getHttpClient()).execute(
				argument.capture(), any(ResponseHandler.class));

		assertEquals(EXPECTED_URI, argument.getValue().getURI().toASCIIString());
	}
	
	private ArtistQueryClient getArtistQueryClient() {
		ArtistQueryClient artistQueryClient = new ArtistQueryClient();
		HttpClient httpClient = Mockito.mock(HttpClient.class);
		artistQueryClient.setHttpClient(httpClient);

		WebserviceHistoryService webserviceHistoryService = 
				Mockito.mock(WebserviceHistoryService.class);
		artistQueryClient.setWebserviceHistoryService(webserviceHistoryService);
		
		return artistQueryClient;
	}

}