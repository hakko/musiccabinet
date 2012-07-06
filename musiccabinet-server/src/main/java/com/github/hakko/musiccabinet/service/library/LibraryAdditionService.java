package com.github.hakko.musiccabinet.service.library;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.FINISHED_MESSAGE;

import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;

import com.github.hakko.musiccabinet.dao.LibraryAdditionDao;
import com.github.hakko.musiccabinet.domain.model.aggr.DirectoryContent;

/*
 * The library scanning is modeled according to the "Pipes and Filters"
 * Enterprise Integration Pattern (EIP), and realized by Spring Integration.
 * 
 * This class acts as a filter that sends messages to a DAO, indicating
 * that files/directories detected as added should be indexed in database.
 */
public class LibraryAdditionService implements LibraryReceiverService {

	protected PollableChannel libraryAdditionChannel;  // consumer of
	
	private LibraryAdditionDao libraryAdditionDao;
	
	public void clearImport() {
		libraryAdditionDao.clearImport();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void receive() {
		Message<DirectoryContent> message;
		while (true) {
			message = (Message<DirectoryContent>) libraryAdditionChannel.receive();
			if (message == null || message.equals(FINISHED_MESSAGE)) {
				libraryAdditionDao.updateLibrary();
				break;
			} else {
				DirectoryContent content = message.getPayload();
				String dir = content.getDirectory();
				libraryAdditionDao.addSubdirectories(dir, content.getSubDirectories());
				libraryAdditionDao.addFiles(dir, content.getFiles());
			}
		}
	}

	public void setLibraryAdditionDao(LibraryAdditionDao libraryAdditionDao) {
		this.libraryAdditionDao = libraryAdditionDao;
	}

	public void setLibraryAdditionChannel(PollableChannel libraryAdditionChannel) {
		this.libraryAdditionChannel = libraryAdditionChannel;
	}

}