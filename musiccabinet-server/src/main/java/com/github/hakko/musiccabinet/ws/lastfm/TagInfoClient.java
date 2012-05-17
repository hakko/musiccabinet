package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.dao.WebserviceHistoryDao;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class TagInfoClient extends AbstractWSClient {

	public static final String METHOD = "tag.getinfo";

	public TagInfoClient() {
		/*
		 * Default behavior is to always check if a call to Last.fm is allowed, by
		 * controlling if an identical call has already been made shortly before.
		 * For tag info, we make an exception, and use a dao that allows every single
		 * call and doesn't log anything. It's OK because tag info is never updated,
		 * it's just called once per tag.
		 */
		setWebserviceHistoryDao(new WebserviceHistoryDao() {
			
			@Override
			public void logWebserviceInvocation(
					WebserviceInvocation webserviceInvocation) {
			}

			@Override
			public void quarantineWebserviceInvocation(
					WebserviceInvocation webserviceInvocation) {
			}

			@Override
			public void blockWebserviceInvocation(
					int artistId, WebserviceInvocation.Calltype callType) {
			}

			@Override
			public boolean isWebserviceInvocationAllowed(
					WebserviceInvocation webserviceInvocation) {
				return true;
			}

			@Override
			public List<Artist> getArtistsScheduledForUpdate(Calltype callType) {
				return null;
			}
			
		});
	}
	
	public WSResponse getTagInfo(String tagName) throws ApplicationException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_TAG, tagName));
		
		return executeWSRequest(null, params);
	}

}