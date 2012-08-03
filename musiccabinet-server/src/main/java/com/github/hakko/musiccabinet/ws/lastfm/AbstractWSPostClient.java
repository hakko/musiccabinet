package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.ws.lastfm.StatusCode.isHttpRecoverable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.util.EntityUtils;

import com.github.hakko.musiccabinet.configuration.CharSet;
import com.github.hakko.musiccabinet.exception.ApplicationException;

/*
 * Base class for all Last.fm web service clients.
 * 
 * Holds common functionality (validating invocation cache, making HTTP request,
 * parsing response envelope, assembling response object with data/error codes).
 */
public abstract class AbstractWSPostClient extends AbstractWSClient {
	
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
	protected WSResponse executeWSRequest(List<NameValuePair> params) throws ApplicationException {
		return invokeCall(params);
	}
	
	/*
	 * Make a single call to a Last.fm web service, and return a packaged result.
	 */
	protected WSResponse invokeSingleCall(List<NameValuePair> params) throws ApplicationException {
		LOG.debug("invokeSingleCall for params");
		WSResponse wsResponse;
		HttpClient httpClient = getHttpClient();
		try {
			HttpPost httpPost = new HttpPost(getURI(params));
			httpPost.setEntity(new UrlEncodedFormEntity(params, CharSet.UTF8));
			LOG.debug("httpPost: " + httpPost);
			for (Header header : httpPost.getAllHeaders()) {
				LOG.debug("httpPost header: " + header);
			}
			LOG.debug("httpPost method: " + httpPost.getMethod());
			LOG.debug("httpPost URI: " + httpPost.getURI());
			for (NameValuePair param : params) {
				LOG.debug("param: " + param);
			}
            LOG.debug("httpClient: " + httpClient + ", manager: " + httpClient.getConnectionManager());
            try {
            	HttpResponse response = httpClient.execute(httpPost);
            	int statusCode = response.getStatusLine().getStatusCode();
            	HttpEntity responseEntity = response.getEntity();
                String responseBody = EntityUtils.toString(responseEntity);
            	LOG.debug("post responseBody: " + responseBody);
            	if (statusCode == 200) {
            		wsResponse = new WSResponse(responseBody);
            	} else {
    				wsResponse = new WSResponse(isHttpRecoverable(statusCode), 
    						statusCode, responseBody);
            	}
            	wsResponse = new WSResponse(responseBody);
            } catch (Throwable t) {
            	LOG.warn("execution post threw exception", t);
            	throw t;
            }
		} catch (ClientProtocolException e) {
			throw new ApplicationException(
					"The request to fetch data from Last.fm could not be completed!", e);
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
			uri = URIUtils.createURI(HTTP, HOST, -1, PATH, null, null);
		} catch (URISyntaxException e) {
			throw new ApplicationException("Could not create Last.fm URI!", e);
		}
		return uri;
	}
	
}