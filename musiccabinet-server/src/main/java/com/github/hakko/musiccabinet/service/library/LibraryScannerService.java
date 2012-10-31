package com.github.hakko.musiccabinet.service.library;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.FINISHED_MESSAGE;
import static com.github.hakko.musiccabinet.service.library.LibraryUtil.msg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.core.PollableChannel;

import com.github.hakko.musiccabinet.domain.model.aggr.SearchIndexUpdateProgress;
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
	private CountDownLatch workerThreads = new CountDownLatch(0);
	
	private LibraryPresenceService libraryPresenceService;
	private LibraryMetadataService libraryMetadataService;
	private LibraryAdditionService libraryAdditionService;
	private LibraryDeletionService libraryDeletionService;
	
	protected String fileSeparator = java.io.File.separator;

	private boolean isLibraryBeingScanned = false;
	
	private static final Logger LOG = Logger.getLogger(LibraryScannerService.class);

	public void add(Set<String> paths) throws ApplicationException {
		update(paths, true);
	}

	public void update(Set<String> paths, boolean isRootPaths) throws ApplicationException {
		isLibraryBeingScanned = true;
		try {
			clearImport();
			startReceivingServices();
			Set<String> rootPaths = getRootPaths(paths);
			for (String path : rootPaths) {
				Files.walkFileTree(Paths.get(path), new LibraryScanner(libraryPresenceChannel));
			}
			if (isRootPaths) {
				libraryPresenceChannel.send(msg(null, rootPaths, new HashSet<File>()));
			}
			libraryPresenceChannel.send(FINISHED_MESSAGE);
			workerThreads.await();
			updateLibrary();
		} catch (IOException | InterruptedException e) {
			throw new ApplicationException("Scanning aborted due to error!", e);
		}
		isLibraryBeingScanned = false;
	}
	
	public void delete(Set<String> paths) throws ApplicationException {
		isLibraryBeingScanned = true;
		libraryDeletionService.delete(paths);
		isLibraryBeingScanned = false;
	}

	/*
	 * If a user attempts to scan both /a and /a/b, silently ignore /a/b
	 * and just scan /a.
	 */
	protected Set<String> getRootPaths(Set<String> paths) {
		List<String> list = new ArrayList<>(paths);
		Collections.sort(list);
		Iterator<String> it = list.iterator();
		for (String prev = null; it.hasNext(); ) {
			String path = it.next();
			if (prev != null && path.startsWith(prev)) {
				it.remove();
			} else {
				prev = path.endsWith(fileSeparator) ? path : (path + fileSeparator);
			}
		}
		return new HashSet<>(list);
	}
	
	public boolean isLibraryBeingScanned() {
		return isLibraryBeingScanned;
	}
	
	public List<SearchIndexUpdateProgress> getUpdateProgress() {
		List<SearchIndexUpdateProgress> updateProgress = new ArrayList<>();
		updateProgress.add(libraryPresenceService.getUpdateProgress());
		updateProgress.add(libraryMetadataService.getUpdateProgress());
		return updateProgress;
	}
	
	private void clearImport() {
		libraryAdditionService.clearImport();
		libraryDeletionService.clearImport();
	}
	
	private void updateLibrary() {
		libraryDeletionService.updateLibrary();
		libraryAdditionService.updateLibrary();
	}
	
	private void startReceivingServices() {
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

	protected void setFileSeparator(String fileSeparator) {
		this.fileSeparator = fileSeparator;
	}
	
}