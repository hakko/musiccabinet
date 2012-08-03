package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class TestWSGetClient extends AbstractWSGetClient {

	private WebserviceInvocation invocation;
	private List<NameValuePair> params;
	
	public TestWSGetClient(WebserviceInvocation invocation, List<NameValuePair> params) {
		this.invocation = invocation;
		this.params = params;
	}
	
	public WSResponse testCall() throws ApplicationException {
		return executeWSRequest(invocation, params);
	}
	
	@Override
	protected long getSleepTime() {
		return 0L;
	}

}