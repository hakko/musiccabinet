package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.exception.ApplicationException;

public class AuthSessionClient extends AbstractWSGetClient {

	public static final String METHOD = "auth.getSession";
	
	public AuthSessionClient() {
		super(WSConfiguration.AUTHENTICATED_UNLOGGED);
	}
	
	public WSResponse getAuthSession(String token) throws ApplicationException {
		List<NameValuePair> params = getDefaultParameterList();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_TOKEN, token));
		
		return executeWSRequest(null, params);
	}
	
}