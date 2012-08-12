package com.github.hakko.musiccabinet.ws.lastfm;

import com.github.hakko.musiccabinet.exception.ApplicationException;

public class RadioPlaylistInvoker {

	public static void main(String[] args) throws ApplicationException {
		
		WSResponse tuneResponse = new RadioTuneClient().tuneToCher();
		
		System.out.println(tuneResponse.getResponseBody());
		System.out.println(tuneResponse.getErrorCode());
		System.out.println(tuneResponse.getErrorMessage());

		WSResponse playlistResponse = new RadioPlaylistClient().getRadioPlaylist();
		
		System.out.println(playlistResponse.getResponseBody());
		System.out.println(playlistResponse.getErrorCode());
		System.out.println(playlistResponse.getErrorMessage());
	}
	
}