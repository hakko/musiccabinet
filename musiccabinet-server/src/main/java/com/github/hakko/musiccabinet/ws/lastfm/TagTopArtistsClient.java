package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.TAG_GET_TOP_ARTISTS;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class TagTopArtistsClient extends AbstractWSGetClient {

	public static final String METHOD = "tag.gettopartists";
	
	public TagTopArtistsClient() {
	}
	
	public WSResponse getTopArtists(Tag tag) throws ApplicationException {
		WebserviceInvocation webserviceInvocation = 
			new WebserviceInvocation(TAG_GET_TOP_ARTISTS, tag);

		List<NameValuePair> params = getDefaultParameterList();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_TAG, tag.getName()));
		
		return executeWSRequest(webserviceInvocation, params);
	}
	
}