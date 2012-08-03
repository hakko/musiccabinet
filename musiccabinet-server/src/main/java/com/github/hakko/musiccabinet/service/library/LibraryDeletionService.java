package com.github.hakko.musiccabinet.service.library;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.FINISHED_MESSAGE;

import java.util.Set;

import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;

import com.github.hakko.musiccabinet.dao.LibraryDeletionDao;
import com.github.hakko.musiccabinet.domain.model.aggr.DirectoryContent;

/*
 * The library scanning is modeled according to the "Pipes and Filters"
 * Enterprise Integration Pattern (EIP), and realized by Spring Integration.
 * 
 * This class acts as a filter that sends messages to a DAO, indicating
 * that files/directories detected as deleted should be removed from database.
 */
public class LibraryDeletionService implements LibraryReceiverService {

	protected PollableChannel libraryDeletionChannel;  // consumer of
	
	private LibraryDeletionDao libraryDeletionDao;

	public void clearImport() {
		libraryDeletionDao.clearImport();
	}
	
	public void updateLibrary() {
		libraryDeletionDao.updateLibrary();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void receive() {
		Message<DirectoryContent> message;
		while (true) {
			message = (Message<DirectoryContent>) libraryDeletionChannel.receive();
			if (message == null || message.equals(FINISHED_MESSAGE)) {
				break;
			} else {
				DirectoryContent content = message.getPayload();
				String dir = content.getDirectory();
				libraryDeletionDao.deleteFiles(dir, content.getFiles());
				libraryDeletionDao.deleteSubdirectories(dir, content.getSubDirectories());
			}
		}
	}
	
	public void delete(Set<String> directories) {
		libraryDeletionDao.deleteSubdirectories(null, directories);
		libraryDeletionDao.updateLibrary();
	}

	public void setLibraryDeletionDao(LibraryDeletionDao libraryDeletionDao) {
		this.libraryDeletionDao = libraryDeletionDao;
	}

	public void setLibraryDeletionChannel(PollableChannel libraryDeletionChannel) {
		this.libraryDeletionChannel = libraryDeletionChannel;
	}

}