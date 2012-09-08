package com.github.hakko.musiccabinet.ws.lastfm;

import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.ALBUM_GET_INFO;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class AlbumInfoClient extends AbstractWSGetClient {

	public static final String METHOD = "album.getinfo";
	
	public WSResponse getAlbumInfo(Album album) throws ApplicationException {
		WebserviceInvocation webserviceInvocation = 
			new WebserviceInvocation(ALBUM_GET_INFO, album);

		List<NameValuePair> params = getDefaultParameterList();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_ARTIST, album.getArtist().getName()));
		params.add(new BasicNameValuePair(PARAM_ALBUM, album.getName()));
		
		return executeWSRequest(webserviceInvocation, params);
	}
	
}