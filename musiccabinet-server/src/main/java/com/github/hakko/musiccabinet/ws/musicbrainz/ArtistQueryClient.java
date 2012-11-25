package com.github.hakko.musiccabinet.ws.musicbrainz;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.MB_ARTIST_QUERY;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;

/*
 * Executes a request to fetch MusicBrainz id for a given artist, i.e:
 * http://musicbrainz.org/ws/2/artist/?query=nirvana&limit=1
 */
public class ArtistQueryClient extends AbstractMusicBrainzClient {

	protected static final String PATH = "/ws/2/artist/";
	
	protected static final String QUERY = "query";
	protected static final String ARTIST = "artist:";
	
	protected static final String LIMIT = "limit";
	protected static final String ONE = "1";
	
	public String get(String artistName) throws ApplicationException {
		WebserviceInvocation invocation = new WebserviceInvocation(
				MB_ARTIST_QUERY, new Artist(artistName));
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(QUERY, ARTIST + escape(artistName)));
		params.add(new BasicNameValuePair(LIMIT, ONE));
		return executeWSRequest(invocation, PATH, params);
	}
	
	private String escape(String artistName) {
		return '"' + StringUtils.replace(artistName, "\"", "\\\"") + '"';
	}
	
}