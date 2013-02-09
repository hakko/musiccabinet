package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.configuration.CharSet.UTF8;
import static com.github.hakko.musiccabinet.service.LastFmService.API_KEY;
import static org.apache.commons.codec.binary.Hex.encodeHex;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.util.ResourceUtil;

public abstract class AbstractWSClient {

	/*
	 * Http client used for actual communication with Last.fm. 
	 * Placed as class variable to allow for unit testing.
	 */
	protected HttpClient httpClient;
	
	public static final String PARAM_API_KEY = "api_key";
	public static final String PARAM_API_SIG = "api_sig";
	public static final String PARAM_ALBUM = "album";
	public static final String PARAM_ARTIST = "artist";
	public static final String PARAM_GROUP = "group";
	public static final String PARAM_LANG = "lang";
	public static final String PARAM_LIMIT = "limit";
	public static final String PARAM_METHOD = "method";
	public static final String PARAM_PAGE = "page";
	public static final String PARAM_PERIOD = "period";
	public static final String PARAM_SK = "sk";
	public static final String PARAM_TAG = "tag";
	public static final String PARAM_TAGS = "tags";
	public static final String PARAM_TIMESTAMP = "timestamp";
	public static final String PARAM_TOKEN = "token";
	public static final String PARAM_TRACK = "track";
	public static final String PARAM_USER = "user";

	/* API sec needed to generate md5 hash for signatures. */
	private static final String API_SEC_RESOURCE = "last.fm/api.sec";
	public static final String API_SEC = new ResourceUtil(API_SEC_RESOURCE).getContent();

	public static final String HTTP = "http";
	public static final String HOST = "ws.audioscrobbler.com";
	public static final String PATH = "/2.0";
	
	protected static final int TIMEOUT = 60 * 1000; // 60 sec

	protected static final Logger LOG = Logger.getLogger(AbstractWSClient.class);
	
	public AbstractWSClient() {
		// default values for a production environment
		httpClient = new DefaultHttpClient();
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, TIMEOUT);
	}

	protected List<NameValuePair> getDefaultParameterList() {
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(PARAM_API_KEY, API_KEY));
		return params;
	}
	
	/*
	 * Assemble URI for the Last.fm web service.
	 */
	protected abstract URI getURI(List<NameValuePair> params) throws ApplicationException;

	protected void authenticateParameterList(List<NameValuePair> params) throws ApplicationException {
		Collections.sort(params, paramComparator);
		StringBuilder sb = new StringBuilder();
		for (NameValuePair param : params) {
			sb.append(param.getName()).append(param.getValue());
		}
		sb.append(API_SEC);
		try {
			MessageDigest md = MessageDigest.getInstance("md5");
			params.add(new BasicNameValuePair(PARAM_API_SIG, new String(
					encodeHex(md.digest(sb.toString().getBytes(UTF8))))));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new ApplicationException("Can not make authenticated call!", e);
		}
	}

	private Comparator<NameValuePair> paramComparator = new Comparator<NameValuePair>() {
		@Override
		public int compare(NameValuePair nvp1, NameValuePair nvp2) {
			return nvp1.getName().compareTo(nvp2.getName());
		}
	};
	
	public void close() {
		httpClient.getConnectionManager().shutdown();
	}
	
	protected HttpClient getHttpClient() {
		return httpClient;
	}

	// Spring setter(s)
	
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

}