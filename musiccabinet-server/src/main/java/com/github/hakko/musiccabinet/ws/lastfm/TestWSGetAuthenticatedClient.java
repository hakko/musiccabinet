package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;

import com.github.hakko.musiccabinet.exception.ApplicationException;

public class TestWSGetAuthenticatedClient extends AbstractWSGetAuthenticatedClient {

	private List<NameValuePair> params;
	
	public TestWSGetAuthenticatedClient(List<NameValuePair> params) {
		this.params = params;
	}
	
	public WSResponse testCall() throws ApplicationException {
		return executeWSRequest(null, params);
	}
	
	@Override
	protected long getSleepTime() {
		return 0L;
	}

}