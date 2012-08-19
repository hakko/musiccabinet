package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import junit.framework.Assert;

import org.apache.http.NameValuePair;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

public class TagTopArtistsClientTest extends AbstractWSImplementationTest {

	@Test
	public void validateParameters() throws ApplicationException {

		final String method = TagTopArtistsClient.METHOD;
		final Tag tag = new Tag(0, "sludge");
		
		new TagTopArtistsClient() {
			@Override
			protected WSResponse executeWSRequest(WebserviceInvocation wi,
					List<NameValuePair> params) throws ApplicationException {
				
				Assert.assertEquals(Calltype.TAG_GET_TOP_ARTISTS, wi.getCallType());
				Assert.assertEquals(tag, wi.getTag());
				
				assertHasParameter(params, PARAM_METHOD, method);
				assertHasParameter(params, PARAM_TAG, tag.getName());
				
				return null;
			}
			
			@Override
			protected WebserviceHistoryService getHistoryService() {
				return Mockito.mock(WebserviceHistoryService.class);
			}

		}.getTopArtists(tag);
	}
	
}