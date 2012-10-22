package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.ws.lastfm.StatusCode.isHttpRecoverable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
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
		authenticateParameterList(params);
		WSResponse wsResponse;
		HttpClient httpClient = getHttpClient();
		try {
			HttpPost httpPost = new HttpPost(getURI(params));
			httpPost.setEntity(new UrlEncodedFormEntity(params, CharSet.UTF8));
			HttpResponse response = httpClient.execute(httpPost);
			int statusCode = response.getStatusLine().getStatusCode();
			HttpEntity responseEntity = response.getEntity();
			String responseBody = EntityUtils.toString(responseEntity);
			EntityUtils.consume(responseEntity);
			LOG.debug("post responseBody: " + responseBody);
			wsResponse = (statusCode == 200) ? 
					new WSResponse(responseBody) :
					new WSResponse(isHttpRecoverable(statusCode), statusCode, responseBody);
		} catch (ClientProtocolException e) {
			throw new ApplicationException(
					"The request to post data to Last.fm could not be completed!", e);
		} catch (IOException e) {
			LOG.warn("Could not post data to Last.fm!", e);
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
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(HTTP);
			uriBuilder.setHost(HOST);
			uriBuilder.setPath(PATH);
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new ApplicationException("Could not create Last.fm URI!", e);
		}
		return uri;
	}
	
}