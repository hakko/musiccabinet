package com.github.hakko.musiccabinet.service.library;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.set;
import static java.io.File.separatorChar;
import static java.lang.Thread.currentThread;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryPresenceDao;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.library.LibraryScannerService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class LibraryScannerServiceTest {

	@Autowired
	private LibraryScannerService scannerService;
	
	@Autowired
	private JdbcLibraryPresenceDao presenceDao;

	@Before
	public void clearLibrary() throws ApplicationException {
		presenceDao.getJdbcTemplate().execute("truncate library.directory cascade");
	}
	
	@Test
	public void traversesLibrary() throws Exception {
		String library = new File(currentThread().getContextClassLoader()
				.getResource("library").toURI()).getAbsolutePath();
		String media1 = library + separatorChar + "media1";
		String artist = media1 + separatorChar + "The Beatles";
		String album = artist + separatorChar + "1962-1966";
		String cd1 = album + separatorChar + "cd1";
		String cd2 = album + separatorChar + "cd2";
		
		scannerService.add(media1);
		Assert.assertEquals(set(album), presenceDao.getSubdirectories(artist));
		Assert.assertEquals(set(cd1, cd2), presenceDao.getSubdirectories(album));
	}

}