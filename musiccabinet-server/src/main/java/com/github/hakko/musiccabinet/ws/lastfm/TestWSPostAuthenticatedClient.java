package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;

import com.github.hakko.musiccabinet.exception.ApplicationException;

public class TestWSPostAuthenticatedClient extends AbstractWSPostAuthenticatedClient {

	private List<NameValuePair> params = getDefaultParameterList();
	
	public TestWSPostAuthenticatedClient(List<NameValuePair> params) {
		this.params.addAll(params);
	}
	
	public List<NameValuePair> getParams() {
		return params;
	}

	public WSResponse testCall() throws ApplicationException {
		return executeWSRequest(params);
	}

}