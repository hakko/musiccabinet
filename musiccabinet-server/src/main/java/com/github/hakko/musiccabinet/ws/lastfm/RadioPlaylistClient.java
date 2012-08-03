package com.github.hakko.musiccabinet.ws.lastfm;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.github.hakko.musiccabinet.exception.ApplicationException;

public class RadioPlaylistClient extends AbstractWSGetAuthenticatedClient {

	public static final String METHOD = "radio.getPlaylist";
	
	public WSResponse getRadioPlaylist() throws ApplicationException {
		List<NameValuePair> params = getDefaultParameterList();
		params.add(new BasicNameValuePair(PARAM_METHOD, METHOD));
		params.add(new BasicNameValuePair(PARAM_SK, "5cd6b262eed1fcecc8752eb78eb1db78"));
		
		return executeWSRequest(null, params);
	}
	
}