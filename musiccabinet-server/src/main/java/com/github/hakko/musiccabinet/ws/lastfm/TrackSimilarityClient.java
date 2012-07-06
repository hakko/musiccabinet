package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.TRACK_GET_SIMILAR;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class TrackSimilarityClient extends AbstractWSClient {

	public static final String METHOD = "track.getsimilar";
	
	public WSResponse getTrackSimilarity(Track track) throws ApplicationException {
		WebserviceInvocation webserviceInvocation = 
			new WebserviceInvocation(TRACK_GET_SIMILAR, track);

		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_ARTIST, track.getArtist().getName()));
		params.add(new BasicNameValuePair(PARAM_TRACK, track.getName()));
		
		return executeWSRequest(webserviceInvocation, params);
	}
	
}