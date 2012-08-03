package com.github.hakko.musiccabinet.io;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.message.GenericMessage;

import com.github.hakko.musiccabinet.domain.model.aggr.DirectoryContent;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.log.Logger;

public class LibraryScanner extends SimpleFileVisitor<Path> {

	private static final Logger LOG = Logger.getLogger(LibraryScanner.class);

	private Map<Path, DirectoryContent> map = new HashMap<>();
	
	private PollableChannel libraryPresenceChannel;
	
    public LibraryScanner(PollableChannel libraryPresenceChannel) {
		this.libraryPresenceChannel = libraryPresenceChannel;
	}

	@Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
    	DirectoryContent parentContent = map.get(dir.getParent());
    	if (parentContent != null) {
    		parentContent.getSubDirectories().add(dir.toString());
    	}
    	map.put(dir, new DirectoryContent(dir.toString()));
    	
    	return CONTINUE;
    }
	
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
    	DirectoryContent directoryContent = map.get(file.getParent());
    	if (attr.size() > Integer.MAX_VALUE) {
    		LOG.warn(file.getFileName() + " has actual file size " + attr.size());
    	}
    	directoryContent.getFiles().add(new File(file.getParent().toString(), 
    			file.getFileName().toString(),
    			new DateTime(attr.lastModifiedTime().toMillis()), 
    			(int) attr.size()));
    	
    	return CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException e) {
    	LOG.warn("Visiting " + file + " failed!", e);
        return CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException e) {
    	DirectoryContent content = map.get(dir);
    	
    	libraryPresenceChannel.send(new GenericMessage<DirectoryContent>(content));
    	
    	map.remove(dir);
    	
    	return CONTINUE;
    }
    
}