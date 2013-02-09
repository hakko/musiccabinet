package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.apache.http.NameValuePair;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.WebserviceHistoryService;

public class ArtistInfoClientTest extends AbstractWSImplementationTest {

	@Test
	public void validateParameters() throws ApplicationException {

		final String method = ArtistInfoClient.METHOD;
		final String artistName = "madonna";
		final String lang = Locale.FRANCE.getLanguage();

		new ArtistInfoClient() {
			@Override
			protected WSResponse executeWSRequest(WebserviceInvocation wi,
					List<NameValuePair> params) throws ApplicationException {
				
				Assert.assertEquals(Calltype.ARTIST_GET_INFO, wi.getCallType());
				Assert.assertTrue(artistName.equals(wi.getArtist().getName()));
				
				assertHasParameter(params, PARAM_METHOD, method);
				assertHasParameter(params, PARAM_ARTIST, artistName);
				assertHasParameter(params, PARAM_LANG, lang);

				return null;
			}
			
			@Override
			protected WebserviceHistoryService getHistoryService() {
				return Mockito.mock(WebserviceHistoryService.class);
			}

		}.getArtistInfo(new Artist(artistName), lang);
	}
	
}