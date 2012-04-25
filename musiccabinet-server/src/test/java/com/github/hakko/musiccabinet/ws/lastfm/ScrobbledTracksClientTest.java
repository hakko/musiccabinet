package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import junit.framework.Assert;

import org.apache.http.NameValuePair;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.hakko.musiccabinet.dao.WebserviceHistoryDao;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class ScrobbledTracksClientTest extends AbstractWSImplementationTest {

	@Test
	public void validateParameters() throws ApplicationException {

		final String method = ScrobbledTracksClient.METHOD;
		final String limit = ScrobbledTracksClient.LIMIT;
		final String user = "ftparea";
		final short page = 3;
		
		new ScrobbledTracksClient() {
			@Override
			protected WSResponse executeWSRequest(WebserviceInvocation wi,
					List<NameValuePair> params) throws ApplicationException {
				
				Assert.assertEquals(Calltype.GET_SCROBBLED_TRACKS, wi.getCallType());
				Assert.assertEquals(Short.valueOf(page), wi.getPage());
				
				assertHasParameter(params, PARAM_METHOD, method);
				assertHasParameter(params, PARAM_LIMIT, limit);
				assertHasParameter(params, PARAM_USER, user);
				assertHasParameter(params, PARAM_PAGE, Short.toString(page));
				
				return null;
			}
			
			@Override
			protected WebserviceHistoryDao getHistoryDao() {
				return Mockito.mock(WebserviceHistoryDao.class);
			}

		}.getLibraryTracks(page, user);
	}
	
}