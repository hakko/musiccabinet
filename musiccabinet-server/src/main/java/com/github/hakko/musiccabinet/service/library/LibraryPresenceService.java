package com.github.hakko.musiccabinet.service.library;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.FINISHED_MESSAGE;
import static com.github.hakko.musiccabinet.service.library.LibraryUtil.msg;
import static com.github.hakko.musiccabinet.service.library.LibraryUtil.removeIntersection;

import java.util.Set;

import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;

import com.github.hakko.musiccabinet.dao.LibraryPresenceDao;
import com.github.hakko.musiccabinet.domain.model.aggr.DirectoryContent;
import com.github.hakko.musiccabinet.domain.model.library.File;

/*
 * The library scanning is modeled according to the "Pipes and Filters"
 * Enterprise Integration Pattern (EIP), and realized by Spring Integration.
 * 
 * This class acts as a filter that determines whether found files and
 * directories are
 *  (1) not changed (then left untouched)
 *  (2) newly added (then passed on to read meta data, then added to db)
 *  (3) delete (then passed to db for removal)
 */
public class LibraryPresenceService implements LibraryReceiverService {

	protected PollableChannel libraryPresenceChannel; // consumer of
	protected PollableChannel libraryMetadataChannel; // producer of
	protected PollableChannel libraryDeletionChannel; // producer of
	
	private LibraryPresenceDao libraryPresenceDao;

	@SuppressWarnings("unchecked")
	@Override
	public void receive() {
		Message<DirectoryContent> message;
		while (true) {
			message = (Message<DirectoryContent>) libraryPresenceChannel.receive();
			if (message == null || message.equals(FINISHED_MESSAGE)) {
				libraryMetadataChannel.send(message);
				libraryDeletionChannel.send(message);
				break;
			} else {
				compareDirectoryContent(message.getPayload());
			}
		}
	}

	protected void compareDirectoryContent(DirectoryContent content) {
		String directory = content.getDirectory();
		Set<File> foundFiles = content.getFiles();
		Set<String> foundSubDirs = content.getSubDirectories();
		Set<File> dbFiles = libraryPresenceDao.getFiles(directory);
		Set<String> dbSubDirs = libraryPresenceDao.getSubdirectories(directory);

		if (!dbSubDirs.equals(foundSubDirs) || !dbFiles.equals(foundFiles)) {
			removeIntersection(dbSubDirs, foundSubDirs);
			removeIntersection(dbFiles, foundFiles);

			if (!foundSubDirs.isEmpty() || !foundFiles.isEmpty()) {
				libraryMetadataChannel.send(msg(directory, foundSubDirs, foundFiles));
			}
			if (!dbSubDirs.isEmpty() || !dbFiles.isEmpty()) {
				libraryDeletionChannel.send(msg(directory, dbSubDirs, dbFiles));
			}
		}
	}

	public void setLibraryPresenceDao(LibraryPresenceDao libraryDao) {
		this.libraryPresenceDao = libraryDao;
	}
	
	public void setLibraryPresenceChannel(PollableChannel libraryPresenceChannel) {
		this.libraryPresenceChannel = libraryPresenceChannel;
	}

	public void setLibraryMetadataChannel(PollableChannel libraryMetadataChannel) {
		this.libraryMetadataChannel = libraryMetadataChannel;
	}

	public void setLibraryDeletionChannel(PollableChannel libraryDeletionChannel) {
		this.libraryDeletionChannel = libraryDeletionChannel;
	}
	
}