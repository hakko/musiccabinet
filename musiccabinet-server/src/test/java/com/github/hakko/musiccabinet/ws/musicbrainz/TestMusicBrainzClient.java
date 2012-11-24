package com.github.hakko.musiccabinet.ws.musicbrainz;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class TestMusicBrainzClient extends AbstractMusicBrainzClient {

	private WebserviceInvocation invocation;
	
	public TestMusicBrainzClient(WebserviceInvocation invocation) {
		this.invocation = invocation;
	}
	
	public String get() throws ApplicationException {
		List<NameValuePair> params = new ArrayList<>();
		return executeWSRequest(invocation, "/test", params);
	}

	public WebserviceInvocation getInvocation() {
		return invocation;
	}

}
