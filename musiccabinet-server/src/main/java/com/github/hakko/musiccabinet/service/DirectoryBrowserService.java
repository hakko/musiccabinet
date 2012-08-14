package com.github.hakko.musiccabinet.service;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.removeIntersection;
import static java.nio.file.Files.getFileAttributeView;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.ApplicationContextException;

import com.github.hakko.musiccabinet.dao.DirectoryBrowserDao;
import com.github.hakko.musiccabinet.dao.LibraryPresenceDao;
import com.github.hakko.musiccabinet.domain.model.aggr.DirectoryContent;
import com.github.hakko.musiccabinet.domain.model.library.Directory;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.music.Album;

public class DirectoryBrowserService {

	private DirectoryBrowserDao dao;
	private LibraryPresenceDao presenceDao;
	
	public Set<Directory> getRootDirectories() {
		return dao.getRootDirectories();
	}
	
	public Directory getDirectory(int directoryId) {
		return dao.getDirectory(directoryId);
	}
	
	public Set<Directory> getSubDirectories(int directoryId) {
		return dao.getSubDirectories(directoryId);
	}
	
	public int getParentId(int directoryId) {
		return dao.getParentId(directoryId);
	}
	
	public DirectoryContent getDirectoryDiff(int directoryId) {
		Directory dir = dao.getDirectory(directoryId);

		Set<String> dbSubDirs = presenceDao.getSubdirectories(dir.getPath());
		Set<File> dbFiles = presenceDao.getFiles(dir.getPath());
		DirectoryContent found = getContent(dir);

		removeIntersection(dbSubDirs, found.getSubDirectories());
		removeIntersection(dbFiles, found.getFiles());
		
		return found;
	}
	
	public void addDirectory(String path, int parentId) {
		dao.addDirectory(path, parentId);
	}
	
	public List<Album> getAlbums(int directoryId, boolean sortAscending) {
		return dao.getAlbums(directoryId, sortAscending);
	}
	
	private DirectoryContent getContent(Directory dir) {
		Set<File> foundFiles = new HashSet<>();
		Set<String> foundSubDirs = new HashSet<>();
		DirectoryContent content = new DirectoryContent(dir.getPath(), foundSubDirs, foundFiles);
		
		Path path = Paths.get(dir.getPath());
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
		    for (Path file: stream) {
		    	BasicFileAttributeView view = getFileAttributeView(file, 
		    			BasicFileAttributeView.class);
		    	BasicFileAttributes attr = view.readAttributes();
		    	if (attr.isDirectory()) {
		    		foundSubDirs.add(file.toAbsolutePath().toString());
		    	} else if (attr.isRegularFile()) {
		    		foundFiles.add(new File(file, attr));
		    	}
		    }
		} catch (IOException | DirectoryIteratorException e) {
			throw new ApplicationContextException("Couldn't read " + dir.getPath(), e);
		}
		
		return content;
	}

	// Spring setters
	
	public void setDirectoryBrowserDao(DirectoryBrowserDao dao) {
		this.dao = dao;
	}

	public void setLibraryPresenceDao(LibraryPresenceDao presenceDao) {
		this.presenceDao = presenceDao;
	}
	
}