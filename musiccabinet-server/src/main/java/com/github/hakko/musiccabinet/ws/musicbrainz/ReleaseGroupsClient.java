package com.github.hakko.musiccabinet.ws.musicbrainz;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.MB_RELEASE_GROUPS;
import static java.lang.String.format;
import static java.lang.String.valueOf;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;

/*
 * Executes a request to fetch MusicBrainz releases for an artist, i.e:
 * http://musicbrainz.org/ws/2/release-group/?query=arid:5b11f4ce-a62d-471e-81fc-a69a8278c7da AND (primarytype:Album OR primarytype:EP OR primarytype:Single) AND NOT secondarytype:compilation AND NOT secondarytype:live AND NOT secondarytype:soundtrack AND status:official&limit=100
 * 
 * Doc: http://musicbrainz.org/doc/Indexed_Search_Syntax 
 * 
 * Not used, @see ReleaseClient
 */
public class ReleaseGroupsClient extends AbstractMusicBrainzClient {

	protected static final String PATH = "/ws/2/release-group/";
	
	protected static final String QUERY = "query";
	protected static final String PATTERN = "arid:%s"
			+ " AND (primarytype:Album OR primarytype:EP OR primarytype:Single)"
			+ " AND NOT secondarytype:compilation"
			+ " AND NOT secondarytype:soundtrack"
			+ " AND NOT secondarytype:spokenword"
			+ " AND NOT secondarytype:interview"
			+ " AND NOT secondarytype:audiobook"
			+ " AND NOT secondarytype:live"
			+ " AND NOT secondarytype:remix"
			+ " AND NOT secondarytype:other"
			+ " AND status:official";
	
	protected static final String LIMIT = "limit";
	protected static final String HUNDRED = "100";
	
	protected static final String OFFSET = "offset";
	
	public String get(String artistName, String mbid, int offset) throws ApplicationException {
		WebserviceInvocation invocation = new WebserviceInvocation(
				MB_RELEASE_GROUPS, new Artist(artistName));
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair(QUERY, format(PATTERN, mbid)));
		params.add(new BasicNameValuePair(LIMIT, HUNDRED));
		params.add(new BasicNameValuePair(OFFSET, valueOf(offset)));
		return executeWSRequest(invocation, PATH, params);
	}
	
}