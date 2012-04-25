package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.GET_SCROBBLED_TRACKS;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class ScrobbledTracksClient extends AbstractWSClient {

	public static final String METHOD = "library.gettracks";
	
	public static final String LIMIT = "1000";
	
	public WSResponse getLibraryTracks(short page, String user) throws ApplicationException {
		WebserviceInvocation webserviceInvocation = 
			new WebserviceInvocation(GET_SCROBBLED_TRACKS, page);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_USER, user));
		params.add(new BasicNameValuePair(PARAM_PAGE, Short.toString(page)));
		params.add(new BasicNameValuePair(PARAM_LIMIT, LIMIT));

		return executeWSRequest(webserviceInvocation, params);
	}
	
}