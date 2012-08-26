package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import junit.framework.Assert;

import org.apache.http.NameValuePair;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.hakko.musiccabinet.dao.LastFmUserDao;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

public class UserRecommendedArtistsClientTest extends AbstractWSImplementationTest {

	@Test
	public void validateParameters() throws ApplicationException {
		
		final String method = UserRecommendedArtistsClient.METHOD;
		final String lastFmUser = "arnathalon";
		final String sessionKey = "sessionkey";
		
		UserRecommendedArtistsClient client = new UserRecommendedArtistsClient() {
			@Override
			protected WSResponse executeWSRequest(WebserviceInvocation wi,
					List<NameValuePair> params) throws ApplicationException {
				
				Assert.assertEquals(Calltype.USER_GET_RECOMMENDED_ARTISTS, wi.getCallType());
				
				assertHasParameter(params, PARAM_METHOD, method);
				assertHasParameter(params, PARAM_LIMIT, "100");
				assertHasParameter(params, PARAM_SK, sessionKey);
				
				return null;
			}
			
		};
		
		client.setWebserviceHistoryService(Mockito.mock(WebserviceHistoryService.class));
		
		LastFmUserDao dao = Mockito.mock(LastFmUserDao.class);
		Mockito.when(dao.getLastFmUser(Mockito.anyString())).thenReturn(
				new LastFmUser(lastFmUser, sessionKey));
		client.setLastFmUserDao(dao);
		
		client.getUserRecommendedArtists(lastFmUser);
	}
	
}