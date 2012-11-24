package com.github.hakko.musiccabinet.ws.musicbrainz;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static org.apache.http.params.HttpConnectionParams.setConnectionTimeout;
import static org.apache.http.params.HttpConnectionParams.setSoTimeout;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

public abstract class AbstractMusicBrainzClient {

	protected HttpClient httpClient; // TODO : move to abstract class for both lastfm/discogs?

	private WebserviceHistoryService webserviceHistoryService;

	protected static final int TIMEOUT = 60 * 1000; // 60 sec
	
	public static final String HTTP = "http";
	public static final String HOST = "musicbrainz.org";
	
	protected static final String USER_AGENT = "User-Agent";
	protected static final String CLIENT_INFO = "MusicCabinet/0.7 ( http://dilerium.se/musiccabinet )";

	// we're only allowed to one call per second
	protected static final long INTERVAL_MS = 1001;
	
	private static final Logger LOG = Logger.getLogger(AbstractMusicBrainzClient.class);
	
	public AbstractMusicBrainzClient() {
		httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		setConnectionTimeout(params, TIMEOUT);
		setSoTimeout(params, TIMEOUT);
	}
	
	protected String executeWSRequest(WebserviceInvocation invocation, 
			String path, List<NameValuePair> params) throws ApplicationException {
		String response = null;
		HttpGet httpGet = new HttpGet(getURI(path, params));
		httpGet.setHeader(USER_AGENT, CLIENT_INFO);
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
        	long elapsedMs = -currentTimeMillis();
			response = httpClient.execute(httpGet, responseHandler);
			elapsedMs += currentTimeMillis();
			sleep(INTERVAL_MS - elapsedMs);
		} catch (HttpResponseException e) {
			LOG.warn(format("MusicBrainz internal error: %d, %s", 
					e.getStatusCode(), e.getMessage()));
			throw new ApplicationException("MusicBrainz internal error!", e);
		} catch (IOException e) {
			throw new ApplicationException("MusicBrainz communication failed!", e);
		} catch (InterruptedException e) {
			LOG.warn("MusicBrainz sleep interrupted!", e);
		}
        webserviceHistoryService.logWebserviceInvocation(invocation);
        return response;
	}

	protected URI getURI(String path, List<NameValuePair> params) throws ApplicationException {
		URI uri = null;
		try {
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(HTTP);
			uriBuilder.setHost(HOST);
			uriBuilder.setPath(path);
			for (NameValuePair param : params) {
				uriBuilder.addParameter(param.getName(), param.getValue());
			}
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new ApplicationException("Could not create MusicBrainz URI!", e);
		}
		return uri;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public WebserviceHistoryService getWebserviceHistoryService() {
		return webserviceHistoryService;
	}

	public void setWebserviceHistoryService(
			WebserviceHistoryService webserviceHistoryService) {
		this.webserviceHistoryService = webserviceHistoryService;
	}
	
}