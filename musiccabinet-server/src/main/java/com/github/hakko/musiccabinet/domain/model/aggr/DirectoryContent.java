package com.github.hakko.musiccabinet.domain.model.aggr;

import java.util.HashSet;
import java.util.Set;

import com.github.hakko.musiccabinet.domain.model.library.File;

/*
 * Represent content found in a directory (names of subdirectories, files).
 * Used for message passing by Spring Integration.
 */
public class DirectoryContent {

	private String directory;
	private Set<String> subDirectories = new HashSet<>();
	private Set<File> files = new HashSet<>();

	public DirectoryContent(String directory) {
		this.directory = directory;
	}
	
	public DirectoryContent(String directory, Set<String> subDirectories, Set<File> files) {
		this.directory = directory;
		this.subDirectories = subDirectories;
		this.files = files;
	}

	public String getDirectory() {
		return directory;
	}

	public Set<String> getSubDirectories() {
		return subDirectories;
	}

	public Set<File> getFiles() {
		return files;
	}
	
	public String toString() {
		return "dir: " + directory + ", subdirs: " + subDirectories;
	}
	
}