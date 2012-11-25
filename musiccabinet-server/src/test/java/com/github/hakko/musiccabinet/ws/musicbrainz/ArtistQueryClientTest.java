package com.github.hakko.musiccabinet.ws.musicbrainz;

import static org.junit.Assert.assertTrue;
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
	public void invokesHttpRequestToExpectedUri() throws Exception {
		verifyUrl("Nirvana", "http://musicbrainz.org/ws/2/artist/?query=artist%3A%22Nirvana%22&limit=1");
	}
	
	@Test
	public void httpRequestIsUTF8Encoded() throws Exception {
		verifyUrl("Säkert!", "%22S%C3%A4kert%21%22");
	}

	@Test
	public void artistNameContainingQuotesIsEscaped() throws Exception {
		verifyUrl("Bonnie \"Prince\" Tyler", "%22Bonnie+%5C%22Prince%5C%22+Tyler%22");
	}
	
	@Test
	public void repeatedCharactersAreUTF8Encoded() throws Exception {
		verifyUrl("Sunn O)))", "Sunn+O%29%29%29");
	}

	@SuppressWarnings("unchecked")
	private void verifyUrl(String artistName, String expectedUrl) throws Exception {
		ArtistQueryClient artistQueryClient = getArtistQueryClient();

		ArgumentCaptor<HttpGet> argument = forClass(HttpGet.class);
		artistQueryClient.get(artistName);
		verify(artistQueryClient.getHttpClient()).execute(
				argument.capture(), any(ResponseHandler.class));
	
		assertTrue("Expected " + expectedUrl + ", got " + argument.getValue().getURI(),
				argument.getValue().getURI().toString().contains(expectedUrl));
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