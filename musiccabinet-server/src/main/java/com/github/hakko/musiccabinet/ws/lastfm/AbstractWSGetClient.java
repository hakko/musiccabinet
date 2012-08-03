package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.configuration.CharSet.UTF8;
import static com.github.hakko.musiccabinet.ws.lastfm.StatusCode.isHttpRecoverable;
import static org.apache.http.client.utils.URLEncodedUtils.format;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.LastFmService;
import com.github.hakko.musiccabinet.service.lastfm.ThrottleService;
import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

/*
 * Base class for all Last.fm web service clients.
 * 
 * Holds common functionality (validating invocation cache, making HTTP request,
 * parsing response envelope, assembling response object with data/error codes).
 */
public abstract class AbstractWSGetClient extends AbstractWSClient {
	
	/*
	 * Interface for controlling if a certain WS invocation would be allowed,
	 * and for logging invocations once successful.
	 * 
	 * This cannot be bypassed since Last.fm Terms of service §4.4 states: 
	 * 
	 * "You agree to cache similar artist and any chart data (top tracks,
	 * top artists, top albums) for a minimum of one week."
	 * 
	 * Placed as class variable to allow for unit testing.
	 */
	private WebserviceHistoryService webserviceHistoryService;
	
	/*
	 * Throttle service responsible for adhering to Last.fm Terms of service
	 * § 4.4, which states:
	 * 
	 * "You will not make more than 5 requests per originating IP address per
	 * second, averaged over a 5 minute period".
	 */
	private ThrottleService throttleService;
	
	/*
	 * Executes a request to a Last.fm web service.
	 * 
	 * When adding support for a new web service, a class extending this should be
	 * implemented. The web service can then be invoked by calling this method, using
	 * relevant parameters.
	 * 
	 * The parameter api_key, which is identical for all web service invocations, is
	 * automatically included.
	 * 
	 * The response is bundled in a WSResponse object, with eventual error code/message.
	 * 
	 * Note: For non US-ASCII characters, Last.fm distinguishes between upper and lower
	 * case. Make sure to use proper capitalization.
	 */
	protected WSResponse executeWSRequest(WebserviceInvocation wi,
			List<NameValuePair> params) throws ApplicationException {
		params.add(new BasicNameValuePair(PARAM_API_KEY, LastFmService.API_KEY));
		WSResponse wsResponse;
		if (getHistoryService().isWebserviceInvocationAllowed(wi)) {
			wsResponse = invokeCall(params);
			if (wsResponse.wasCallSuccessful()) {
				getHistoryService().logWebserviceInvocation(wi);
			} else if (!wsResponse.isErrorRecoverable()) {
				getHistoryService().quarantineWebserviceInvocation(wi);
			} else {
				LOG.warn("Couldn't invoke " + wi + ", response: " + wsResponse);
			}
		} else {
			wsResponse = new WSResponse();
		}
		return wsResponse;
	}
	
	/*
	 * Make a single call to a Last.fm web service, and return a packaged result.
	 */
	protected WSResponse invokeSingleCall(List<NameValuePair> params) throws ApplicationException {
		if (throttleService != null) {
			throttleService.awaitAllowance();
		}
		WSResponse wsResponse;
		HttpClient httpClient = getHttpClient();
		try {
			HttpGet httpGet = new HttpGet(getURI(params));
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpClient.execute(httpGet, responseHandler);
            wsResponse = new WSResponse(responseBody);
		} catch (ClientProtocolException e) {
			if (e instanceof HttpResponseException) {
				int statusCode = ((HttpResponseException) e).getStatusCode();
				wsResponse = new WSResponse(isHttpRecoverable(statusCode), 
						statusCode, e.getMessage());
			} else {
				throw new ApplicationException(
					"The request to fetch data from Last.fm could not be completed!", e);
			}
		} catch (IOException e) {
			LOG.warn("Could not fetch data from Last.fm!", e);
			wsResponse = new WSResponse(true, -1, "Call failed due to " + e.getMessage());
		}
		return wsResponse;
	}

	/*
	 * Assemble URI for the Last.fm web service.
	 */
	protected URI getURI(List<NameValuePair> params) throws ApplicationException {
		URI uri = null;
		try {
			uri = URIUtils.createURI(HTTP, HOST, -1, PATH, format(params, UTF8), null);
		} catch (URISyntaxException e) {
			throw new ApplicationException("Could not create Last.fm URI!", e);
		}
		return uri;
	}
	
	protected WebserviceHistoryService getHistoryService() {
		return webserviceHistoryService;
	}

	// Spring setters

	public void setWebserviceHistoryService(WebserviceHistoryService historyService) {
		this.webserviceHistoryService = historyService;
	}

	public void setThrottleService(ThrottleService throttleService) {
		this.throttleService = throttleService;
	}
	
}