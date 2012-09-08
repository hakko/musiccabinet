package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.USER_GET_TOP_ARTISTS;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.Period;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class UserTopArtistsClient extends AbstractWSGetClient {

	public static final String METHOD = "user.gettopartists";
	
	public WSResponse getUserTopArtists(LastFmUser user, Period period) throws ApplicationException {
		WebserviceInvocation webserviceInvocation = 
			new WebserviceInvocation(USER_GET_TOP_ARTISTS, user, period.getDays());

		List<NameValuePair> params = getDefaultParameterList();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_USER, user.getLastFmUsername()));
		params.add(new BasicNameValuePair(PARAM_PERIOD, period.getDescription()));
		
		return executeWSRequest(webserviceInvocation, params);
	}
	
}