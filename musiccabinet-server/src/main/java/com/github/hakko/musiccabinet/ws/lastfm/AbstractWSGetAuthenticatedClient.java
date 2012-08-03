package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

/*
 * Base class for all Last.fm web service clients, that needs authentication.
 * 
 * Extends AbstractWSGetClient by adding a calculated api_sig parameter, based
 * on passed parameters.
 */
public abstract class AbstractWSGetAuthenticatedClient extends AbstractWSGetClient {
	
	protected WSResponse executeWSRequest(WebserviceInvocation wi, List<NameValuePair> params) throws ApplicationException {
		authenticateParameterList(params);
		WSResponse wsResponse = invokeCall(params);
		if (!wsResponse.wasCallSuccessful()) {
			LOG.warn("Couldn't invoke " + wi + ", response: " + wsResponse);
		}
		return wsResponse;
	}
	
}