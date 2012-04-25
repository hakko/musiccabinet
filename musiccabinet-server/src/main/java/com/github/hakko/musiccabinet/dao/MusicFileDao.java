package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;

public interface MusicFileDao {

	void clearImport();
	void addMusicFiles(List<MusicFile> musicFiles);
	void createMusicFiles();
	void createMusicFileInternalIds();
	
	List<MusicFile> getMusicFiles();

	int getTrackId(String path) throws ApplicationException;
	Track getTrack(String path);
	
}