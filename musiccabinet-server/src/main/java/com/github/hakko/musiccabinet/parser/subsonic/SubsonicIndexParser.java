package com.github.hakko.musiccabinet.parser.subsonic;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.library.MusicDirectory;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public interface SubsonicIndexParser {

	boolean readBatch() throws ApplicationException;
	List<MusicFile> getMusicFiles();
	List<MusicDirectory> getMusicDirectories();
	
}