package com.github.hakko.musiccabinet.dao;

import java.util.List;

import com.github.hakko.musiccabinet.domain.model.library.MusicDirectory;

public interface MusicDirectoryDao {

	void addMusicDirectories(List<MusicDirectory> musicDirectories);
	void createMusicDirectories();
	void clearImport();
	
	List<MusicDirectory> getMusicDirectories();
	
	Integer getArtistId(String path);
	
}