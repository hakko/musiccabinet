package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import junit.framework.Assert;

import org.apache.http.NameValuePair;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

public class GroupWeeklyArtistChartClientTest extends AbstractWSImplementationTest {

	@Test
	public void validateParameters() throws ApplicationException {

		final String method = GroupWeeklyArtistChartClient.METHOD;
		final String lastFmGroup = "Brainwashed";
		
		new GroupWeeklyArtistChartClient() {
			@Override
			protected WSResponse executeWSRequest(WebserviceInvocation wi,
					List<NameValuePair> params) throws ApplicationException {
				
				Assert.assertEquals(Calltype.GROUP_WEEKLY_ARTIST_CHART, wi.getCallType());
				Assert.assertEquals(lastFmGroup, wi.getGroup().getName());
				
				assertHasParameter(params, PARAM_METHOD, method);
				assertHasParameter(params, PARAM_GROUP, lastFmGroup);
				
				return null;
			}
			
			@Override
			protected WebserviceHistoryService getHistoryService() {
				return Mockito.mock(WebserviceHistoryService.class);
			}

		}.getWeeklyArtistChart(new LastFmGroup(lastFmGroup));
	}
	
}