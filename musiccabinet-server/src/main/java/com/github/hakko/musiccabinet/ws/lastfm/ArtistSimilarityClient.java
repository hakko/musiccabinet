package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.ARTIST_GET_SIMILAR;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class ArtistSimilarityClient extends AbstractWSClient {

	public static final String METHOD = "artist.getsimilar";
	
	public ArtistSimilarityClient() {
	}
	
	public WSResponse getArtistSimilarity(Artist artist) throws ApplicationException {
		WebserviceInvocation webserviceInvocation = 
			new WebserviceInvocation(ARTIST_GET_SIMILAR, artist);

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_ARTIST, artist.getName()));
		
		return executeWSRequest(webserviceInvocation, params);
	}
	
}