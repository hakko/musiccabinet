package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.USER_GET_LOVED_TRACKS;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class UserLovedTracksClient extends AbstractWSGetClient {

	public static final String METHOD = "user.getLovedTracks";
	
	public static final String LIMIT = "100";

	public WSResponse getUserLovedTracks(LastFmUser user, short page) throws ApplicationException {
		WebserviceInvocation webserviceInvocation = 
			new WebserviceInvocation(USER_GET_LOVED_TRACKS, user, page);

		List<NameValuePair> params = getDefaultParameterList();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_LIMIT, LIMIT));
		params.add(new BasicNameValuePair(PARAM_USER, user.getLastFmUsername()));
		params.add(new BasicNameValuePair(PARAM_PAGE, Short.toString(page)));
		
		return executeWSRequest(webserviceInvocation, params);
	}
	
}