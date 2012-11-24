package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;

import com.github.hakko.musiccabinet.exception.ApplicationException;

public class TestWSGetAuthenticatedClient extends AbstractWSGetClient {

	private List<NameValuePair> params;
	
	public TestWSGetAuthenticatedClient(List<NameValuePair> params) {
		super(WSConfiguration.AUTHENTICATED_UNLOGGED_TEST);
		this.params = params;
	}
	
	public WSResponse testCall() throws ApplicationException {
		return executeWSRequest(null, params);
	}

}