package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.exception.ApplicationException;

public class TagInfoClient extends AbstractWSGetClient {

	public static final String METHOD = "tag.getinfo";

	/*
	 * Default behavior is to always check if a call to Last.fm is allowed, by
	 * controlling if an identical call has already been made shortly before.
	 * For tag info, we make an exception, allow every single call and doesn't
	 * log anything. It's OK because tag info is never updated, it's just called
	 * once per tag.
	 */
	public WSResponse getTagInfo(String tagName) throws ApplicationException {
		List<NameValuePair> params = getDefaultParameterList();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_TAG, tagName));
		
		return executeWSRequest(null, params);
	}

}