package com.github.hakko.musiccabinet.service.library;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.FINISHED_MESSAGE;
import static com.github.hakko.musiccabinet.service.library.LibraryUtil.msg;
import static com.github.hakko.musiccabinet.service.library.LibraryUtil.set;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.PollableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryAdditionDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryPresenceDao;
import com.github.hakko.musiccabinet.domain.model.library.File;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class LibraryDeletionServiceTest {

	@Autowired
	private LibraryDeletionService deletionService;

	@Autowired
	private JdbcLibraryAdditionDao additionDao;

	@Autowired
	private JdbcLibraryPresenceDao presenceDao;
	
	private String dir1 = "/dir1", dir2 = "/dir1/dir2";
	private File file1a = getFile(dir1, "file1a");
	private File file1b = getFile(dir1, "file1b");
	private File file2a = getFile(dir2, "file2a");
	
	@Before
	public void clearLibrary() {
		presenceDao.getJdbcTemplate().execute("truncate library.directory cascade");
		presenceDao.getJdbcTemplate().execute("truncate music.artist cascade");
		
		additionDao.clearImport();
		additionDao.addSubdirectories(dir1, set(dir2));
		additionDao.addSubdirectories(null, set(dir1));
		additionDao.addFiles(dir2, set(file2a));
		additionDao.addFiles(dir1, set(file1a, file1b));
		additionDao.updateLibrary();
	}
	
	@Test
	public void deletesNestedDirectoriesAndFiles() {
		
		PollableChannel deletionChannel = deletionService.libraryDeletionChannel;
		deletionChannel.send(msg(dir1, set(dir2), set(file1a)));
		deletionChannel.send(FINISHED_MESSAGE);
		
		deletionService.receive();
		deletionService.updateLibrary();
		
		assertEquals(set(file1b), presenceDao.getFiles(dir1));
		assertFalse(presenceDao.exists(dir2));
	}
	
}