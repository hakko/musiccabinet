package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import junit.framework.Assert;

import org.apache.http.NameValuePair;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.hakko.musiccabinet.domain.model.library.Period;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

public class UserTopArtistsClientTest extends AbstractWSImplementationTest {

	@Test
	public void validateParameters() throws ApplicationException {

		final String method = UserTopArtistsClient.METHOD;
		final String lastFmUser = "arnathalon";
		final Period period = Period.OVERALL;
		
		new UserTopArtistsClient() {
			@Override
			protected WSResponse executeWSRequest(WebserviceInvocation wi,
					List<NameValuePair> params) throws ApplicationException {
				
				Assert.assertEquals(Calltype.USER_GET_TOP_ARTISTS, wi.getCallType());
				Assert.assertEquals(lastFmUser, wi.getUser().getLastFmUsername());
				
				assertHasParameter(params, PARAM_METHOD, method);
				assertHasParameter(params, PARAM_USER, lastFmUser);
				assertHasParameter(params, PARAM_PERIOD, period.getDescription());
				
				return null;
			}
			
			@Override
			protected WebserviceHistoryService getHistoryService() {
				return Mockito.mock(WebserviceHistoryService.class);
			}

		}.getUserTopArtists(new LastFmUser(lastFmUser), period);
	}
	
}