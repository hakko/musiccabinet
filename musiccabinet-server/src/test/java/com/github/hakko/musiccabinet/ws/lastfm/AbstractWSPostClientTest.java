package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.service.LastFmService.API_KEY;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.HOST;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.HTTP;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.PARAM_API_KEY;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.PARAM_API_SIG;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.PARAM_METHOD;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.PATH;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class AbstractWSPostClientTest extends AbstractWSImplementationTest {

	private static final String FAILED_KEY_RESOURCE =
			"last.fm/xml/failed-key.xml";

	@Test
	public void validateURIBuilder() throws ApplicationException {
		URI uri = new AbstractWSPostClient() {
		}.getURI(new ArrayList<NameValuePair>());
		String expected = HTTP + "://" + HOST + PATH;

		Assert.assertNotNull(uri);
		Assert.assertEquals(expected, uri.toString());
	}

	@Test
	public void apiKeyGetsAddedAndSignatureGenerated() throws ApplicationException, IOException {
		final String method = "test.call";
		
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(PARAM_METHOD, method));

		TestWSPostAuthenticatedClient client = getTestWSClient(params, FAILED_KEY_RESOURCE);
		params = client.getParams();
		
		client.executeWSRequest(params);
		
		Assert.assertNotNull(params);
		Assert.assertEquals(3, params.size());
		
		assertHasParameter(params, PARAM_API_KEY, API_KEY);
		assertHasParameter(params, PARAM_METHOD, method);
		Assert.assertEquals(PARAM_API_SIG, params.get(2).getName());
	}
	
	/*
	 * Help method to create a base TestWSClient.
	 */
	private TestWSPostAuthenticatedClient getTestWSClient(List<NameValuePair> params, 
			String responseURI) throws IOException {
		HttpClient httpClient = Mockito.mock(HttpClient.class);
		ClientConnectionManager connectionManager = Mockito.mock(
				ClientConnectionManager.class);
		when(httpClient.getConnectionManager()).thenReturn(connectionManager);

		StatusLine statusLine = Mockito.mock(StatusLine.class);
		when(statusLine.getStatusCode()).thenReturn(200);
		
		HttpEntity httpEntity = Mockito.mock(HttpEntity.class);
		when(httpEntity.getContent()).thenReturn(new ResourceUtil(responseURI).getInputStream());
		
		HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(httpResponse.getEntity()).thenReturn(httpEntity);
		
		when(httpClient.execute(Mockito.any(HttpUriRequest.class))).thenReturn(httpResponse);
		
		TestWSPostAuthenticatedClient testClient = new TestWSPostAuthenticatedClient(params);
		testClient.setHttpClient(httpClient);

		return testClient;
	}

}