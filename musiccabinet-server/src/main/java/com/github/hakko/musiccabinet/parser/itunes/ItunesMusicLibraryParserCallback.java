package com.github.hakko.musiccabinet.parser.itunes;

import com.github.hakko.musiccabinet.parser.itunes.ItunesMusicLibraryParserImpl.ItunesTrack;

public interface ItunesMusicLibraryParserCallback {

	void addTrack(ItunesTrack iTunesTrack);
	void endOfTracks();
	
}