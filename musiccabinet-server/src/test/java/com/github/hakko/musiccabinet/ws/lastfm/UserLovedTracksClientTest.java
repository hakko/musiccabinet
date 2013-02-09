package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import junit.framework.Assert;

import org.apache.http.NameValuePair;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

public class UserLovedTracksClientTest extends AbstractWSImplementationTest {

	@Test
	public void validateParameters() throws ApplicationException {

		final String method = UserLovedTracksClient.METHOD;
		final String limit = UserLovedTracksClient.LIMIT;
		final LastFmUser lastFmUser = new LastFmUser("rj");
		final short page = 4;
		
		new UserLovedTracksClient() {
			@Override
			protected WSResponse executeWSRequest(WebserviceInvocation wi,
					List<NameValuePair> params) throws ApplicationException {
				
				Assert.assertEquals(Calltype.USER_GET_LOVED_TRACKS, wi.getCallType());
				Assert.assertEquals(lastFmUser, wi.getUser());
				
				assertHasParameter(params, PARAM_METHOD, method);
				assertHasParameter(params, PARAM_LIMIT, limit);
				assertHasParameter(params, PARAM_USER, lastFmUser.getLastFmUsername());
				assertHasParameter(params, PARAM_PAGE, Short.toString(page));
				
				return null;
			}
			
			@Override
			protected WebserviceHistoryService getHistoryService() {
				return Mockito.mock(WebserviceHistoryService.class);
			}

		}.getUserLovedTracks(lastFmUser, page);
	}
	
}