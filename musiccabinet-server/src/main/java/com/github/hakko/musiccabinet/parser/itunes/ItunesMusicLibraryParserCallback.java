package com.github.hakko.musiccabinet.parser.itunes;

import com.github.hakko.musiccabinet.domain.model.library.MusicFile;

public interface ItunesMusicLibraryParserCallback {

	void addMusicFile(MusicFile musicFile);
	void endOfMusicFiles();
	
}