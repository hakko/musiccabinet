package com.github.hakko.musiccabinet.ws.musicbrainz;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

public class AbstractMusicBrainzClientTest {
	
	@Test (expected = ApplicationException.class)
	@SuppressWarnings("unchecked")
	public void failsWithApplicationExceptionOnIOException() throws Exception {
		TestMusicBrainzClient client = getClient();
		when(client.getHttpClient().execute(any(HttpUriRequest.class), 
				any(ResponseHandler.class))).thenThrow(new IOException("Interrupted"));

		client.get();
	}
	
	@Test (expected = ApplicationException.class)
	@SuppressWarnings("unchecked")
	public void failsWithApplicationExceptionOnInternalErrors() throws Exception {
		TestMusicBrainzClient client = getClient();
		when(client.getHttpClient().execute(any(HttpUriRequest.class), 
				any(ResponseHandler.class))).thenThrow(new HttpResponseException(503, "NA"));

		client.get();
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void throttlesToOneCallPerSecond() throws Exception {
		TestMusicBrainzClient client = getClient();
		when(client.getHttpClient().execute(any(HttpUriRequest.class), 
				any(ResponseHandler.class))).thenReturn("OK");

		long ms = -System.currentTimeMillis();
		client.get();
		ms += System.currentTimeMillis();

		assertTrue("Web service only took " + ms + " ms", ms > 1000);
	}
		
	@Test
	public void logsSuccessfulCall() throws ApplicationException {
		TestMusicBrainzClient client = getClient();

		WebserviceInvocation invocation = client.getInvocation();

		verify(client.getWebserviceHistoryService(), never()).logWebserviceInvocation(invocation);
		client.get();
		verify(client.getWebserviceHistoryService()).logWebserviceInvocation(invocation);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void doesntLogFailedCall() throws Exception {
		TestMusicBrainzClient client = getClient();
		when(client.getHttpClient().execute(any(HttpUriRequest.class), 
				any(ResponseHandler.class))).thenThrow(new IOException("Interrupted"));

		WebserviceInvocation invocation = client.getInvocation();
		try {
			client.get();
		} catch (ApplicationException e) {
			verify(client.getWebserviceHistoryService(), never()).logWebserviceInvocation(invocation);
			return;
		}
		
		Assert.fail();
	}
	
	private TestMusicBrainzClient getClient() {
		WebserviceInvocation invocation = Mockito.mock(WebserviceInvocation.class);
		TestMusicBrainzClient client = new TestMusicBrainzClient(invocation);
		HttpClient httpClient = Mockito.mock(HttpClient.class);
		client.setHttpClient(httpClient);

		WebserviceHistoryService webserviceHistoryService = 
				Mockito.mock(WebserviceHistoryService.class);
		client.setWebserviceHistoryService(webserviceHistoryService);
		
		return client;
	}

}