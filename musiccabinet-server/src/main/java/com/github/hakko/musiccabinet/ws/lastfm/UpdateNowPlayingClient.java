package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.domain.model.aggr.Scrobble;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public class UpdateNowPlayingClient extends AbstractWSPostClient {

	public static final String METHOD = "track.updateNowPlaying";

	public WSResponse updateNowPlaying(Scrobble scrobble) throws ApplicationException {
		List<NameValuePair> params = getDefaultParameterList();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_ARTIST, scrobble.getTrack().getArtist().getName()));
		params.add(new BasicNameValuePair(PARAM_ALBUM, scrobble.getTrack().getMetaData().getAlbum()));
		params.add(new BasicNameValuePair(PARAM_TRACK, scrobble.getTrack().getName()));
		params.add(new BasicNameValuePair(PARAM_DURATION, "" + scrobble.getTrack().getMetaData().getDuration()));
		params.add(new BasicNameValuePair(PARAM_SK, scrobble.getLastFmUser().getSessionKey()));

		return executeWSRequest(params);
	}

}