package com.github.hakko.musiccabinet.ws.lastfm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;

import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistInfoParserImpl;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTracksParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.util.StringUtil;

public class WSResponseTest {

	private static final String TOP_TRACKS_RESOURCE = "last.fm/xml/toptracks.cher.xml";
	private static final String FAILED_TRACK_RESOURCE = "last.fm/xml/failed-track.xml";
	private static final String FAILED_KEY_RESOURCE = "last.fm/xml/failed-key.xml";

	private static final String CTRL_CHAR_RESPONSE_1 = "last.fm/xml/artistinfo.bachelorette.xml";
	private static final String CTRL_CHAR_RESPONSE_2 = "last.fm/xml/toptracks.paulkelly.xml";

	@Test (expected = ApplicationException.class)
	public void nullResponseThrowsException() throws ApplicationException {
		new WSResponse(null);
	}

	@Test (expected = ApplicationException.class)
	public void emptyResponseThrowsException() throws ApplicationException {
		new WSResponse("");
	}
	
	@Test
	public void unallowedCallGetsWrappedNicely() {
		WSResponse response = new WSResponse();
		
		Assert.assertFalse(response.wasCallAllowed());
	}

	@Test
	public void recoverableHttpErrorGetsWrappedNicely() {
		boolean recoverable = true;
		int errorCode = 503;
		String errorMessage = "Service temporary unavaible";
		
		WSResponse response = new WSResponse(recoverable, errorCode, errorMessage);
		
		Assert.assertTrue(response.wasCallAllowed());
		Assert.assertFalse(response.wasCallSuccessful());
		Assert.assertEquals(recoverable, response.isErrorRecoverable());
		Assert.assertEquals(errorCode, response.getErrorCode());
		Assert.assertEquals(errorMessage, response.getErrorMessage());
	}

	@Test
	public void unrecoverableHttpErrorGetsWrappedNicely() {
		boolean recoverable = false;
		int errorCode = 404;
		String errorMessage = "Not found";
		
		WSResponse response = new WSResponse(recoverable, errorCode, errorMessage);
		
		Assert.assertTrue(response.wasCallAllowed());
		Assert.assertFalse(response.wasCallSuccessful());
		Assert.assertEquals(recoverable, response.isErrorRecoverable());
		Assert.assertEquals(errorCode, response.getErrorCode());
		Assert.assertEquals(errorMessage, response.getErrorMessage());
	}

	@Test
	public void normalResponseValidatesCorrectly() throws ApplicationException, IOException {
		String normalResponse = new ResourceUtil(TOP_TRACKS_RESOURCE).getContent();
		WSResponse response = new WSResponse(normalResponse);
		
		assertTrue(response.wasCallSuccessful());
		assertEquals(normalResponse, response.getResponseBody());
	}

	@Test
	public void failedTrackResponseValidatesCorrectly() throws ApplicationException, IOException {
		String failedResponse = new ResourceUtil(FAILED_TRACK_RESOURCE).getContent();
		WSResponse response = new WSResponse(failedResponse);
		
		assertFalse(response.wasCallSuccessful());
		assertEquals(6, response.getErrorCode());
		assertEquals("Track not found", response.getErrorMessage());
	}

	@Test
	public void failedKeyResponseValidatesCorrectly() throws ApplicationException, IOException {
		String failedResponse = new ResourceUtil(FAILED_KEY_RESOURCE).getContent();
		WSResponse response = new WSResponse(failedResponse);
		
		assertFalse(response.wasCallSuccessful());
		assertEquals(26, response.getErrorCode());
		assertEquals("Suspended API key", response.getErrorMessage());
	}
	
	/*
	 * Use an authentic response from last.fm, and assert that WSResponse
	 * silently replaces illegal XML characters in it.
	 */
	@Test
	public void illegalControlCharactersAreChomped1() throws ApplicationException {
		String ctrlCharResponse = new ResourceUtil(CTRL_CHAR_RESPONSE_1).getContent();
		WSResponse response = new WSResponse(ctrlCharResponse);

		// supposed to work, as WSResponse chomps illegal control characters
		new ArtistInfoParserImpl(new StringUtil(response.getResponseBody()).getInputStream());
		
		try {
			// supposed to fail, as it hasn't passed WSResponse
			new ArtistInfoParserImpl(new ResourceUtil(CTRL_CHAR_RESPONSE_1).getInputStream());
			Assert.fail();
		} catch (ApplicationException e) {
			
		}
	}

	/*
	 * Use an authentic response from last.fm, and assert that WSResponse
	 * silently replaces illegal XML characters in it.
	 */
	@Test
	public void illegalControlCharactersAreChomped2() throws ApplicationException {
		String ctrlCharResponse = new ResourceUtil(CTRL_CHAR_RESPONSE_2).getContent();
		WSResponse response = new WSResponse(ctrlCharResponse);

		// supposed to work, as WSResponse chomps illegal control characters
		new ArtistTopTracksParserImpl(new StringUtil(response.getResponseBody()).getInputStream());
		
		try {
			// supposed to fail, as it hasn't passed WSResponse
			new ArtistTopTracksParserImpl(new ResourceUtil(CTRL_CHAR_RESPONSE_2).getInputStream());
			Assert.fail();
		} catch (ApplicationException e) {
			
		}
	}

	@Test (expected = ApplicationException.class)
	public void malformedEnvelopeThrowsException() throws ApplicationException {
		new WSResponse("no data");
	}

	@Test (expected = ApplicationException.class)
	public void malformedErrorBodyThrowsException() throws ApplicationException {
		new WSResponse("<lfm status=\"failed\"><unexpectedResponse/></lfm>");
	}

}