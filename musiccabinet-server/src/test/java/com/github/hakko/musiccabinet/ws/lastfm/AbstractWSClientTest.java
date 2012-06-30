package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.configuration.CharSet.UTF8;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.API_KEY;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.API_KEY_RESOURCE;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.HOST;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.HTTP;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.PARAM_API_KEY;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.PARAM_ARTIST;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.PARAM_TRACK;
import static com.github.hakko.musiccabinet.ws.lastfm.AbstractWSClient.PATH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.hakko.musiccabinet.dao.WebserviceHistoryDao;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.ThrottleService;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public class AbstractWSClientTest {
	
	private static final String SIMILAR_TRACKS_RESOURCE = 
		"last.fm/xml/similartracks.cher.believe.xml";
	private static final String FAILED_KEY_RESOURCE =
		"last.fm/xml/failed-key.xml";
	
	@Test
	public void testdataFoundOnClasspath() {
		for (String resource : Arrays.asList(API_KEY_RESOURCE,
				SIMILAR_TRACKS_RESOURCE, FAILED_KEY_RESOURCE)) {
			new ResourceUtil(resource);
		}
	}
	
	@Test
	public void validateURIBuilder() throws ApplicationException {
		final String artistName = "Madonna";
		final String trackName = "Holiday";
		NameValuePair artist = new BasicNameValuePair(PARAM_ARTIST, artistName);
		NameValuePair track = new BasicNameValuePair(PARAM_TRACK, trackName);
		NameValuePair api_key = new BasicNameValuePair(PARAM_API_KEY, API_KEY);
		URI uri = AbstractWSClient.getURI(Arrays.asList(artist, track, api_key));
		String expected = HTTP + "://" + HOST + PATH 
		+ "?" + PARAM_ARTIST + "=" + artistName
		+ "&" + PARAM_TRACK + "=" + trackName
		+ "&" + PARAM_API_KEY + "=" + API_KEY;

		Assert.assertNotNull(uri);
		Assert.assertEquals(expected, uri.toString());
	}

	@Test
	public void validateURIBuilderForSpecialChars() throws ApplicationException, UnsupportedEncodingException {
		final String artistName = "Sonny & Cher";
		final String trackName = "I Got You Babe";
		NameValuePair artist = new BasicNameValuePair(PARAM_ARTIST, artistName);
		NameValuePair track = new BasicNameValuePair(PARAM_TRACK, trackName);
		NameValuePair api_key = new BasicNameValuePair(PARAM_API_KEY, API_KEY);
		URI uri = AbstractWSClient.getURI(Arrays.asList(artist, track, api_key));
		String expected = HTTP + "://" + HOST + PATH 
		+ "?" + PARAM_ARTIST + "=" + URLEncoder.encode(artistName, UTF8)
		+ "&" + PARAM_TRACK + "=" + URLEncoder.encode(trackName, UTF8)
		+ "&" + PARAM_API_KEY + "=" + API_KEY;

		Assert.assertNotNull(uri);
		Assert.assertEquals(expected, uri.toString());
	}

	@Test
	public void validatePackagingOfFailedCall() throws IOException, ApplicationException {
		TestWSClient testWSClient = getTestWSClient(true, FAILED_KEY_RESOURCE);
		WSResponse wsResponse = testWSClient.testCall();
		testWSClient.close();
		
		Assert.assertTrue(wsResponse.wasCallAllowed());
		Assert.assertFalse(wsResponse.wasCallSuccessful());
		Assert.assertEquals(26, wsResponse.getErrorCode());
		Assert.assertEquals("Suspended API key", wsResponse.getErrorMessage());
	}

	@Test
	public void validatePackagingOfSuccessfulCall() throws IOException, ApplicationException {
		TestWSClient testWSClient = getTestWSClient(true, SIMILAR_TRACKS_RESOURCE);
		WSResponse wsResponse = testWSClient.testCall();
		testWSClient.close();
		
		Assert.assertTrue(wsResponse.wasCallAllowed());
		Assert.assertTrue(wsResponse.wasCallSuccessful());
		Assert.assertEquals(wsResponse.getResponseBody(), 
				new ResourceUtil(SIMILAR_TRACKS_RESOURCE).getContent());
	}

	@Test
	public void validatePackagingOfDisallowedCall() throws IOException, ApplicationException {
		TestWSClient testWSClient = getTestWSClient(false, SIMILAR_TRACKS_RESOURCE);
		WSResponse wsResponse = testWSClient.testCall();
		testWSClient.close();
		
		Assert.assertFalse(wsResponse.wasCallAllowed());
	}
	
	@Test (expected = ApplicationException.class)
	public void validatePackagingOfClientProtocalException() throws ApplicationException, IOException {
		ClientProtocolException cpe = new ClientProtocolException("Unknown protocol");
		TestWSClient testWSClient = getTestWSClient(cpe);
		testWSClient.testCall();
	}

	@Test
	public void validatePackagingOfResponseCode404() throws ApplicationException, IOException {
		final int errorCode = 404;
		final String errorMessage = "Not found";
		HttpResponseException hpe = new HttpResponseException(errorCode, errorMessage);
		TestWSClient testWSClient = getTestWSClient(hpe);
		WSResponse response = testWSClient.testCall();
		
		Assert.assertTrue(response.wasCallAllowed());
		Assert.assertFalse(response.wasCallSuccessful());
		Assert.assertFalse(response.isErrorRecoverable());
		Assert.assertEquals(errorCode, response.getErrorCode());
		Assert.assertEquals(errorMessage, response.getErrorMessage());
	}

	@Test
	public void validatePackagingOfResponseCode503() throws ApplicationException, IOException {
		final int errorCode = 503;
		final String errorMessage = "Service temporary unavailable";
		HttpResponseException hpe = new HttpResponseException(errorCode, errorMessage);
		TestWSClient testWSClient = getTestWSClient(hpe);
		WSResponse response = testWSClient.testCall();
		
		Assert.assertTrue(response.wasCallAllowed());
		Assert.assertFalse(response.wasCallSuccessful());
		Assert.assertTrue(response.isErrorRecoverable());
		Assert.assertEquals(errorCode, response.getErrorCode());
		Assert.assertEquals(errorMessage, response.getErrorMessage());
	}

	@Test
	public void validatePackagingOfIOException() throws ApplicationException, IOException {
		IOException ioe = new IOException("Communication interrupted");
		TestWSClient testWSClient = getTestWSClient(ioe);
		WSResponse response = testWSClient.testCall();
		
		Assert.assertTrue(response.wasCallAllowed());
		Assert.assertFalse(response.wasCallSuccessful());
		Assert.assertTrue(response.isErrorRecoverable());
	}

	/*
	 * Creates and returns a TestWSClient that returns a mocked response body.
	 */
	@SuppressWarnings("unchecked")
	private TestWSClient getTestWSClient(
			boolean allowCalls, String responseURI) throws IOException {
		TestWSClient testWSClient = getTestWSClient(allowCalls);
		HttpClient httpClient = testWSClient.getHttpClient();
		String httpResponse = new ResourceUtil(responseURI).getContent();
		when(httpClient.execute(Mockito.any(HttpUriRequest.class), 
				Mockito.any(ResponseHandler.class))).thenReturn(httpResponse);
		return testWSClient;
	}
	
	/*
	 * Creates and returns a TestWSClient that throws an exception on invocation.
	 */
	@SuppressWarnings("unchecked")
	private TestWSClient getTestWSClient(Exception e) throws IOException {
		TestWSClient testWSClient = getTestWSClient(true);
		HttpClient httpClient = testWSClient.getHttpClient();
		when(httpClient.execute(Mockito.any(HttpUriRequest.class),
				Mockito.any(ResponseHandler.class))).thenThrow(e);
		return testWSClient;
	}
	
	/*
	 * Help method to create a base TestWSClient.
	 */
	private TestWSClient getTestWSClient(boolean allowCalls) {
		HttpClient httpClient = Mockito.mock(HttpClient.class);
		ClientConnectionManager connectionManager = Mockito.mock(
				ClientConnectionManager.class);
		when(httpClient.getConnectionManager()).thenReturn(connectionManager);
		
		WebserviceHistoryDao historyDao = mock(WebserviceHistoryDao.class);
		when(historyDao.isWebserviceInvocationAllowed(
				Mockito.any(WebserviceInvocation.class))).thenReturn(allowCalls);
		
		WebserviceInvocation invocation = mock(WebserviceInvocation.class);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		TestWSClient testClient = new TestWSClient(invocation, params);
		testClient.setWebserviceHistoryDao(historyDao);
		testClient.setHttpClient(httpClient);
		
		// create a throttling service that allows calls at any rate
		ThrottleService throttleService = mock(ThrottleService.class);
		testClient.setThrottleService(throttleService);

		return testClient;
	}

}