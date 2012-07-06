package com.github.hakko.musiccabinet.dao;

import java.util.Set;

import com.github.hakko.musiccabinet.domain.model.library.File;

public interface LibraryPresenceDao {

	boolean exists(String directory);
	Set<String> getSubdirectories(String directory);
	Set<File> getFiles(String directory);
	
}