package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.Arrays;
import java.util.List;

public class StatusCode {

	private StatusCode() {}
	
	/*
	 * The following errors are considered recoverable, i.e, if we try calling again
	 * in a couple of minutes, we might be more successful.
	 * 
	 * 11 : Service Offline - This service is temporarily offline. Try again later.
	 * 16 : There was a temporary error processing your request. Please try again
	 * 29 : Rate limit exceeded - Your IP has made too many requests in a short period
	 * (although we do take care to make 29 not happen)
	 */
	private static final List<Integer> recoverableLastfmErrorCodes = 
		Arrays.asList(11, 16, 29);

	/*
	 * Same as above.
	 * 
	 * 503: Temp Unavailable
	 */
	private static final List<Integer> recoverableHttpErrorCodes = 
		Arrays.asList(503);

	/*
	 * Returns whether a supplied Last.fm status code should be treated as recoverable.
	 */
	public static boolean isLastfmRecoverable(int statusCode) {
		return recoverableLastfmErrorCodes.contains(statusCode);
	}

	/*
	 * Returns whether a supplied http status code should be treated as recoverable.
	 */
	public static boolean isHttpRecoverable(int statusCode) {
		return recoverableHttpErrorCodes.contains(statusCode);
	}
	
}