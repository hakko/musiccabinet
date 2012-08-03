package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.service.LastFmService.API_KEY;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import junit.framework.Assert;

import org.apache.http.NameValuePair;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

public class AuthSessionClientTest extends AbstractWSImplementationTest {

	@Test
	public void validateParameters() throws ApplicationException, NoSuchAlgorithmException, UnsupportedEncodingException {
		
		final String method = AuthSessionClient.METHOD;
		final String token = "token";
		
		new AuthSessionClient() {
			@Override
			protected WSResponse executeWSRequest(WebserviceInvocation wi,
					List<NameValuePair> params) throws ApplicationException {
				
				Assert.assertEquals(null, wi);
				
				assertHasParameter(params, PARAM_METHOD, method);
				assertHasParameter(params, PARAM_TOKEN, token);
				assertHasParameter(params, PARAM_API_KEY, API_KEY);
				
				return null;
			}
			
			@Override
			protected WebserviceHistoryService getHistoryService() {
				return Mockito.mock(WebserviceHistoryService.class);
			}

		}.getAuthSession(token);
	}
	
}