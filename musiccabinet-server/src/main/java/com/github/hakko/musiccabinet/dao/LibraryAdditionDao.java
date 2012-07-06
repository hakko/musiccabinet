package com.github.hakko.musiccabinet.dao;

import java.util.Set;

import com.github.hakko.musiccabinet.domain.model.library.File;

public interface LibraryAdditionDao {

	void clearImport();

	void addSubdirectories(String directory, Set<String> subDirectories);
	void addFiles(String directory, Set<File> files);
	
	void updateLibrary();
	
}