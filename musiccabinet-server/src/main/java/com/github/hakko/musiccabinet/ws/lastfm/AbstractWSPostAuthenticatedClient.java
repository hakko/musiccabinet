package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;

import com.github.hakko.musiccabinet.exception.ApplicationException;

/*
 * Base class for all Last.fm web service clients, that needs authentication.
 * 
 * Extends AbstractWSPostClient by adding a calculated api_sig parameter, based
 * on passed parameters.
 */
public abstract class AbstractWSPostAuthenticatedClient extends AbstractWSPostClient {
	
	protected WSResponse executeWSRequest(List<NameValuePair> params) throws ApplicationException {
		authenticateParameterList(params);
		return super.executeWSRequest(params);
	}

	@Override
	protected int getCallAttempts() {
		return 1;
	}
	
	@Override
	public long getSleepTime() {
		return 0;
	}
	
}