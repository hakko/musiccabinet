package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.ws.lastfm.StatusCode.isLastfmRecoverable;
import static org.apache.commons.lang.math.NumberUtils.toInt;

import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;

/*
 * Wraps all possible responses from a Last.fm web service request, including:
 * 
 * - web service call is not allowed (by our own logging framework)
 * - web service call returned a HTTP error.
 * - web service call returned an envelope indicating an error
 * - web service call returned actual data.
 * 
 * When handling a WSResponse, check:
 * - wasCallAllowed()
 * - wasCallSuccessful()
 *  - if no, check isErrorRecoverable()
 *   - if yes, sleep and try again
 *   - if no, use getErrorCode() and getErrorMessage()
 *  - if yes, check getResponseBody()
 *  
 */
public class WSResponse {

	private final boolean callAllowed;
	private String responseBody;
	private boolean callSuccessful;
	private int errorCode;
	private String errorMessage;
	private boolean errorRecoverable;
	
	private static final Logger LOG = Logger.getLogger(WSResponse.class);

	public static final String RESPONSE_OK = "<lfm status=\"ok\">";
	public static final String RESPONSE_FAILED = "<lfm status=\"failed\">";

	/*
	 * Used to wrap a response where no call was invoked (call not allowed,
	 * most probably because an identical call was made just recently)
	 */
	public WSResponse() {
		this.callAllowed = false;
	}
	
	/*
	 * Used when calling the web service was allowed, but rather than getting
	 * an xml response, we received a transport layer error.
	 */
	public WSResponse(boolean errorRecoverable, int errorCode, String errorMessage) {
		this.callAllowed = true;
		this.callSuccessful = false;
		this.errorRecoverable = errorRecoverable;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/*
	 * Used when calling the web service was allowed,
	 * we received a response and now we want to parse it.
	 * 
	 * Some caution has to be taken as last.fm sometimes
	 * include illegal control characters in the response.
	 * They are silently removed to allow for parsing.
	 */
	public WSResponse(String responseBody) throws ApplicationException {
		this.callAllowed = true;
		if (responseBody != null) {
			char[] chars = responseBody.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
				if (Character.isISOControl(c) && c != 0x09
						&& c != 0x0A && c != 0x0D) {
					chars[i] = ' ';
				}
			}
			this.responseBody = new String(chars);
		}
		validateResponse();
	}
	
	/*
	 * Validates the response.
	 * 
	 * If the service responsed with a <lfm status="ok">, validation quietly stops.
	 * 
	 * If the service responded with a <lfm status="failed">, errorCode and errorMessage
	 * are populated from response.
	 * 
	 * Otherwise, a general ApplicationException is thrown.
	 */
	private void validateResponse() throws ApplicationException {
		if (responseBody == null || responseBody.isEmpty()) {
			throw new ApplicationException(
					"The response from Last.fm did not contain any data!");
		}
		if (responseBody.indexOf(RESPONSE_OK) != -1) {
			callSuccessful = true;
			return;
		}
		if (responseBody.indexOf(RESPONSE_FAILED) != -1) {
			parseErrorCodeAndMessage();
		} else {
			throw new ApplicationException(
					"The response from Last.fm wasn't wrapped as promised!");
		}
	}
	
	/*
	 * Response is on the following format:
	 * 
	 * <?xml version="1.0" encoding="utf-8"?>
	 * <lfm status="failed">
	 * <error code="6">Track not found</error></lfm>
	 * 
	 * Don't bother to invoke an xml parser, just iterate over the character array
	 * and keep track of position indexes for the values we're looking for.
	 * 
	 */
	private void parseErrorCodeAndMessage() throws ApplicationException {
		char[] chars = responseBody.toCharArray();
		int quoteCount = 0, openTagCount = 0, closeTagCount = 0;
		int errorCodeStart = 0, errorCodeEnd = 0, errorMsgStart = 0, errorMsgEnd = 0;

		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '"') {
				++quoteCount;
				if (quoteCount == 7) {
					errorCodeStart = i + 1;
				} else if (quoteCount == 8) {
					errorCodeEnd = i;
				}
			} else if (chars[i] == '>' && ++closeTagCount == 3) {
				errorMsgStart = i + 1;
			} else if (chars[i] == '<' && ++openTagCount == 4) {
				errorMsgEnd = i;
			}
		}
		
		if (errorCodeStart * errorCodeEnd * errorMsgStart * errorMsgEnd == 0) {
			LOG.info(responseBody);
			throw new ApplicationException("Response from Last.fm not properly formed!");
		}
		
		errorCode = toInt(responseBody.substring(errorCodeStart, errorCodeEnd));
		errorMessage = responseBody.substring(errorMsgStart, errorMsgEnd);
		errorRecoverable = isLastfmRecoverable(errorCode);
	}

	/*
	 * Returns whether we're allowed to make this call to Last.fm. Set based on if
	 * an identical call has already been made within a certain time frame.
	 */
	public boolean wasCallAllowed() {
		return callAllowed;
	}
	
	/*
	 * Actual web service response, minus http headers.
	 */
	public String getResponseBody() {
		return responseBody;
	}

	/*
	 * Returns whether Last.fm wrapped this in a "status=ok" envelope.
	 */
	public boolean wasCallSuccessful() {
		return callSuccessful;
	}

	/*
	 * Returns error code signaled in http headers or in Last.fm service envelope.
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/*
	 * Returns a descriptive error message in accordance with error code.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/*
	 * Returns whether it's worth making the same call again in a little while.
	 */
	public boolean isErrorRecoverable() {
		return errorRecoverable;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("allowed: " + callAllowed);
		sb.append(", successful: " + callSuccessful);
		sb.append(", errorCode: " + errorCode);
		sb.append(", errorMessage: " + errorMessage);
		sb.append(", errorRecoverable: " + errorRecoverable);
		sb.append(", responseBody: " + responseBody);
		return sb.toString();
	}
}