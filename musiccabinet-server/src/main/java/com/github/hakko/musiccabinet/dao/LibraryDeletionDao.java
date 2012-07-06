package com.github.hakko.musiccabinet.dao;

import java.util.Set;

import com.github.hakko.musiccabinet.domain.model.library.File;

public interface LibraryDeletionDao {

	void clearImport();

	void deleteSubdirectories(String directory, Set<String> subDirectories);
	void deleteFiles(String directory, Set<File> files);
	
	void updateLibrary();

}