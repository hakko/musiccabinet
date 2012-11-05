package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.aggr.ArtistUserTag;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class TagUpdateClient extends AbstractWSPostClient {

	public static final String ADD_METHOD = "artist.addTags";
	public static final String REMOVE_METHOD = "artist.removeTag";

	public WSResponse updateTag(ArtistUserTag artistUserTag) throws ApplicationException {
		List<NameValuePair> params = getDefaultParameterList();
		params.add(new BasicNameValuePair(PARAM_ARTIST, artistUserTag.getArtist().getName()));
		params.add(new BasicNameValuePair(PARAM_SK, artistUserTag.getLastFmUser().getSessionKey()));
		if (artistUserTag.getTagOccurrence().isUse()) {
			params.add(new BasicNameValuePair(PARAM_METHOD, ADD_METHOD));
			params.add(new BasicNameValuePair(PARAM_TAGS, artistUserTag.getTagOccurrence().getTag()));
		} else {
			params.add(new BasicNameValuePair(PARAM_METHOD, REMOVE_METHOD));
			params.add(new BasicNameValuePair(PARAM_TAG, artistUserTag.getTagOccurrence().getTag()));
		}
		
		return executeWSRequest(params);
	}
	
}