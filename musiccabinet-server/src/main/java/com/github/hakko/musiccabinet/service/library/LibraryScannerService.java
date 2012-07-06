package com.github.hakko.musiccabinet.service.library;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.FINISHED_MESSAGE;
import static com.github.hakko.musiccabinet.service.library.LibraryUtil.msg;
import static com.github.hakko.musiccabinet.service.library.LibraryUtil.set;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.core.PollableChannel;

import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.io.LibraryScanner;
import com.github.hakko.musiccabinet.log.Logger;

/*
 * The library scanning is modeled according to the "Pipes and Filters"
 * Enterprise Integration Pattern (EIP), and realized by Spring Integration.
 * 
 * This class acts as a starting point that initiates all filters (running
 * them as separate threads), and sends out a finishing message when the
 * scanning is done.
 */
public class LibraryScannerService {

	private PollableChannel libraryPresenceChannel; // producer of
	
	private TaskExecutor taskExecutor;
	private CountDownLatch workerThreads;
	
	private LibraryPresenceService libraryPresenceService;
	private LibraryMetadataService libraryMetadataService;
	private LibraryAdditionService libraryAdditionService;
	private LibraryDeletionService libraryDeletionService;
	
	private static final Logger LOG = Logger.getLogger(LibraryScannerService.class);
	
	public void add(String... paths) throws ApplicationException {
		try {
			clearImport();
			startReceivingServices();
			for (String path : paths) {
				Files.walkFileTree(Paths.get(path), new LibraryScanner(libraryPresenceChannel));
			}
			libraryPresenceChannel.send(msg(null, set(paths), new HashSet<File>()));
			libraryPresenceChannel.send(FINISHED_MESSAGE);
			workerThreads.await();
		} catch (IOException | InterruptedException e) {
			throw new ApplicationException("Scanning aborted due to error!", e);
		}
	}
	
	public void delete(String... paths) throws ApplicationException {
		libraryDeletionService.delete(paths);
	}

	public void clearImport() {
		libraryAdditionService.clearImport();
		libraryDeletionService.clearImport();
	}
	
	public void startReceivingServices() {
		List<LibraryReceiverService> libraryReceiverServices = new ArrayList<>();
		libraryReceiverServices.add(libraryPresenceService);
		libraryReceiverServices.add(libraryMetadataService);
		libraryReceiverServices.add(libraryAdditionService);
		libraryReceiverServices.add(libraryDeletionService);
		workerThreads = new CountDownLatch(libraryReceiverServices.size());
		for (final LibraryReceiverService libraryReceiverService : libraryReceiverServices) {
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						libraryReceiverService.receive();
					} catch (Throwable t) {
						LOG.error("Unexpected error caught during scanning!", t);
						t.printStackTrace();
					} finally {
						workerThreads.countDown();
					}
				}
			});
		}
	}

	public void setLibraryPresenceChannel(PollableChannel libraryPresenceChannel) {
		this.libraryPresenceChannel = libraryPresenceChannel;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public void setLibraryPresenceService(LibraryPresenceService libraryPresenceService) {
		this.libraryPresenceService = libraryPresenceService;
	}

	public void setLibraryMetadataService(LibraryMetadataService libraryMetadataService) {
		this.libraryMetadataService = libraryMetadataService;
	}

	public void setLibraryAdditionService(LibraryAdditionService libraryAdditionService) {
		this.libraryAdditionService = libraryAdditionService;
	}

	public void setLibraryDeletionService(LibraryDeletionService libraryDeletionService) {
		this.libraryDeletionService = libraryDeletionService;
	}
	
}