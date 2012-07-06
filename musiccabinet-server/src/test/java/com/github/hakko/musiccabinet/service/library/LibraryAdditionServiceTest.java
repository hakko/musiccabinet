package com.github.hakko.musiccabinet.service.library;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.FINISHED_MESSAGE;
import static com.github.hakko.musiccabinet.service.library.LibraryUtil.msg;
import static com.github.hakko.musiccabinet.service.library.LibraryUtil.set;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.PollableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryPresenceDao;
import com.github.hakko.musiccabinet.domain.model.library.File;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class LibraryAdditionServiceTest {

	@Autowired
	private LibraryAdditionService additionService;

	@Autowired
	private JdbcLibraryPresenceDao presenceDao;
	
	private String dir1 = "/dir1", dir2 = "/dir1/dir2";
	private File file1a = new File(dir1, "file1a", NOW, 0);
	private File file1b = new File(dir1, "file1b", NOW, 0);
	private File file2a = new File(dir2, "file2a", NOW, 0);
	
	private static final DateTime NOW = DateTime.now();
	
	@Before
	public void clearLibrary() {
		presenceDao.getJdbcTemplate().execute("truncate library.directory cascade");
	}
	
	@Test
	public void addsNestedDirectoriesAndFiles() {
		additionService.clearImport();
		
		PollableChannel additionChannel = additionService.libraryAdditionChannel;
		additionChannel.send(msg(dir2, new HashSet<String>(), set(file2a)));
		additionChannel.send(msg(dir1, set(dir2), set(file1a, file1b)));
		additionChannel.send(msg(null, set(dir1), new HashSet<File>()));
		additionChannel.send(FINISHED_MESSAGE);
		
		additionService.receive();
		
		assertEquals(set(file1a, file1b), presenceDao.getFiles(dir1));
		assertEquals(set(file2a), presenceDao.getFiles(dir2));
	}
	
}