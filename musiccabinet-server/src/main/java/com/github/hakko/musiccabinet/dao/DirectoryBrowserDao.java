package com.github.hakko.musiccabinet.dao;

import java.util.List;
import java.util.Set;

import com.github.hakko.musiccabinet.domain.model.library.Directory;
import com.github.hakko.musiccabinet.domain.model.music.Album;

public interface DirectoryBrowserDao {

	Set<Directory> getRootDirectories();
	Directory getDirectory(int directoryId);
	Set<Directory> getSubDirectories(int directoryId);
	int getParentId(int directoryId);
	void addDirectory(String path, int parentId);
	List<Album> getAlbums(int directoryId, boolean sortAscending);
	List<String> getNonAudioFiles(int directoryId);
	
}
