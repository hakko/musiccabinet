package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class TrackUnLoveClient extends AbstractWSPostClient {

	public static final String METHOD = "track.unlove";

	public WSResponse love(Track track, LastFmUser lastFmUser) throws ApplicationException {
		List<NameValuePair> params = getDefaultParameterList();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_TRACK, track.getName()));
		params.add(new BasicNameValuePair(PARAM_ARTIST, track.getArtist().getName()));
		params.add(new BasicNameValuePair(PARAM_SK, lastFmUser.getSessionKey()));

		return executeWSRequest(params);
	}

}