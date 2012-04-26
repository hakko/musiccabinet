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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.github.hakko.musiccabinet.dao.WebserviceHistoryDao;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.service.ThrottleService;
import com.github.hakko.musiccabinet.util.ResourceUtil;

/*
 * Base class for all Last.fm web service clients.
 * 
 * Holds common functionality (validating invocation cache, making HTTP request,
 * parsing response envelope, assembling response object with data/error codes).
 */
public abstract class AbstractWSClient {
	
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
	private WebserviceHistoryDao historyDao;

	/*
	 * Http client used for actual communication with Last.fm. 
	 * Placed as class variable to allow for unit testing.
	 */
	private HttpClient httpClient;
	
	/*
	 * Throttle service responsible for adhering to Last.fm Terms of service
	 * § 4.4, which states:
	 * 
	 * "You will not make more than 5 requests per originating IP address per
	 * second, averaged over a 5 minute period".
	 */
	private ThrottleService throttleService;
	
	/* API key needed to identify this project when communicating with Last.fm. */
	public static final String API_KEY_RESOURCE = "last.fm/api.key";
	public static final String API_KEY = new ResourceUtil(API_KEY_RESOURCE).getContent();

	public static final String PARAM_METHOD = "method";
	public static final String PARAM_ARTIST = "artist";
	public static final String PARAM_ALBUM = "album";
	public static final String PARAM_TRACK = "track";
	public static final String PARAM_API_KEY = "api_key";
	public static final String PARAM_LIMIT = "limit";
	public static final String PARAM_PAGE = "page";
	public static final String PARAM_USER = "user";
	public static final String PARAM_TAG = "tag";
	
	public static final String HTTP = "http";
	public static final String HOST = "ws.audioscrobbler.com";
	public static final String PATH = "/2.0";
	
	private static final int TIMEOUT = 60 * 1000; // 60 sec

	private static final Logger LOG = Logger.getLogger(AbstractWSClient.class);
	
	public AbstractWSClient() {
		// default values for a production environment
		httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT);
	}
	
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
		WSResponse wsResponse;
		if (getHistoryDao().isWebserviceInvocationAllowed(wi)) {
			wsResponse = invokeCall(params);
			if (wsResponse.wasCallSuccessful()) {
				getHistoryDao().logWebserviceInvocation(wi);
			} else if (!wsResponse.isErrorRecoverable()) {
				getHistoryDao().quarantineWebserviceInvocation(wi);
			} else {
				LOG.warn("Couldn't invoke " + wi + ", response: " + wsResponse);
			}
		} else {
			wsResponse = new WSResponse();
		}
		return wsResponse;
	}

	/*
	 * Try calling the web service. If invocation fails but it is marked as
	 * recoverable, sleep for five minutes and try again until fifteen
	 * minutes has passed. Then give up.
	 */
	private WSResponse invokeCall(List<NameValuePair> params) throws ApplicationException {
		WSResponse wsResponse = null;
		int callAttempts = 0;
		while (++callAttempts <= 3) {
			wsResponse = invokeSingleCall(params);
			if (wsResponse.wasCallSuccessful()) {
				break;
			}
			if (!wsResponse.isErrorRecoverable()) {
				break;
			}
			try {
				Thread.sleep(getSleepTime());
			} catch (InterruptedException e) {
				// we can't do much about this
			}
		}
		return wsResponse;
	}

	/*
	 * Placed here to allow sub-classes to override (for unit testing).
	 */
	protected long getSleepTime() {
		return 1000 * 60 * 5;
	}
	
	/*
	 * Make a single call to a Last.fm web service, and return a packaged result.
	 */
	private WSResponse invokeSingleCall(List<NameValuePair> params) throws ApplicationException {
		throttleService.awaitAllowance();
		WSResponse wsResponse;
		HttpClient httpClient = getHttpClient();
		try {
			params.add(new BasicNameValuePair(PARAM_API_KEY, API_KEY));
			HttpGet httpGet = new HttpGet(getURI(params));
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            long ms = -System.currentTimeMillis();
            String responseBody = httpClient.execute(httpGet, responseHandler);
            ms += System.currentTimeMillis();

            StringBuilder sb = new StringBuilder();
            for (NameValuePair nvp : params) {
            	if (!nvp.getName().equals(PARAM_API_KEY))
            	sb.append(nvp.getValue() + "\t");
            }
            LOG.debug(sb.toString() + "\t" + ms + " ms");
            
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
	
	protected HttpClient getHttpClient() {
		return httpClient;
	}
	
	protected WebserviceHistoryDao getHistoryDao() {
		return historyDao;
	}
	
	/*
	 * Assemble URI for the Last.fm web service.
	 */
	protected static URI getURI(List<NameValuePair> params) throws ApplicationException {
		URI uri = null;
		try {
			uri = URIUtils.createURI(HTTP, HOST, -1, PATH, format(params, UTF8), null);
		} catch (URISyntaxException e) {
			throw new ApplicationException("Could not create Last.fm URI!", e);
		}
		return uri;
	}

	public void close() {
		httpClient.getConnectionManager().shutdown();
	}

	// Spring setters

	public void setWebserviceHistoryDao(WebserviceHistoryDao historyDao) {
		this.historyDao = historyDao;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public void setThrottleService(ThrottleService throttleService) {
		this.throttleService = throttleService;
	}
	
}