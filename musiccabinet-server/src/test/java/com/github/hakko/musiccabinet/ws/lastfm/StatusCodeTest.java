package com.github.hakko.musiccabinet.ws.lastfm;

import junit.framework.Assert;

import org.junit.Test;

public class StatusCodeTest {
	
	@Test
	public void validateRecoverableHTTPResponses() {
		Assert.assertFalse(StatusCode.isHttpRecoverable(-1));
		Assert.assertFalse(StatusCode.isHttpRecoverable(404)); // not found
		Assert.assertFalse(StatusCode.isHttpRecoverable(418)); // teapot
		
		Assert.assertTrue(StatusCode.isHttpRecoverable(503)); // temp. unavailable
	}
	
}