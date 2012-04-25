package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.ARTIST_GET_TOP_TRACKS;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class ArtistTopTracksClient extends AbstractWSClient {

	public static final String METHOD = "artist.gettoptracks";
	
	public WSResponse getTopTracks(Artist artist) throws ApplicationException {
		WebserviceInvocation webserviceInvocation = 
			new WebserviceInvocation(ARTIST_GET_TOP_TRACKS, artist);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_ARTIST, artist.getName()));
		// tried adding autocorrect=1, but even simple misspellings didn't get corrected
		
		return executeWSRequest(webserviceInvocation, params);
	}
	
}