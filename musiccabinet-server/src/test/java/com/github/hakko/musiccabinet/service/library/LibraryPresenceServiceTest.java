package com.github.hakko.musiccabinet.service.library;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.FINISHED_MESSAGE;
import static com.github.hakko.musiccabinet.service.library.LibraryUtil.set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.LibraryPresenceDao;
import com.github.hakko.musiccabinet.domain.model.aggr.DirectoryContent;
import com.github.hakko.musiccabinet.domain.model.library.File;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class LibraryPresenceServiceTest {

	@Autowired
	private LibraryPresenceService presenceService;
	
	private String dir1 = "/d1";
	private String dir2 = "/d1/d2";
	
	private File file1 = new File(dir1, "f1", new DateTime(), 0);
	private File file2 = new File(dir1, "f2", new DateTime(), 0);
	private File file3 = new File(dir1, "f3", new DateTime(), 0);

	private File file2b = new File(dir1, "f2", new DateTime(), 1); // changed version of file2

	@Test
	public void delegatesHandlingOfAddedAndDeletedResources() {
		LibraryPresenceDao presenceDao = mock(LibraryPresenceDao.class);
		when(presenceDao.getFiles(dir1)).thenReturn(set(file1, file2, file3));
		when(presenceDao.getSubdirectories(dir1)).thenReturn(set(dir2));
		presenceService.setLibraryPresenceDao(presenceDao);

		PollableChannel presenceChannel = presenceService.libraryPresenceChannel;
		presenceChannel.send(LibraryUtil.msg(dir1, set(dir2), set(file1, file2b)));
		presenceChannel.send(FINISHED_MESSAGE);
		
		presenceService.receive();
		
		Message<?> additionMessage, deletionMessage;
		assertNotNull(additionMessage = presenceService.libraryMetadataChannel.receive());
		assertNotNull(deletionMessage = presenceService.libraryDeletionChannel.receive());

		assertEquals(FINISHED_MESSAGE, presenceService.libraryMetadataChannel.receive());
		assertEquals(FINISHED_MESSAGE, presenceService.libraryDeletionChannel.receive());

		Set<File> addedFiles = ((DirectoryContent) additionMessage.getPayload()).getFiles();
		Set<File> deletedFiles = ((DirectoryContent) deletionMessage.getPayload()).getFiles();

		assertEquals(set(file2b), addedFiles);
		assertEquals(set(file2, file3), deletedFiles);
	}
	
}