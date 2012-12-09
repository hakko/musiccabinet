package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_STATISTICS;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;
import static java.io.File.separatorChar;
import static junit.framework.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.LibraryStatistics;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.library.LibraryUtilTest;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcLibraryBrowserDaoTest {
	
	@Autowired
	private JdbcLibraryBrowserDao browserDao;

	@Autowired
	private JdbcLibraryAdditionDao additionDao;
	
	@Before
	public void loadFunctionDependency() {
		browserDao.getJdbcTemplate().execute("truncate music.artist cascade");
		browserDao.getJdbcTemplate().execute("truncate library.file cascade");
	}
	
	@Test
	public void updatesStatistics() throws ApplicationException {
		PostgreSQLUtil.loadFunction(browserDao, UPDATE_STATISTICS);

		File f1 = getFile("A", "A", "t1", 829, 54);
		File f2 = getFile("A", "B", "t2",  29, 42);
		File f3 = getFile("B", "C", "t3", 823, 30);

		submitFile(additionDao, f1);
		assertEquals(new LibraryStatistics(1, 1, 1, 829, 54), getStats());

		submitFile(additionDao, f2);
		assertEquals(new LibraryStatistics(1, 2, 2, 829+29, 54+42), getStats());

		submitFile(additionDao, f3);
		assertEquals(new LibraryStatistics(2, 3, 3, 829+29+823, 54+42+30), getStats());
	}

	private File getFile(String artist, String album, String title, int size, int seconds) {
		File f = UnittestLibraryUtil.getFile(artist, album, title);
		f.setSize(size);
		f.getMetadata().setDuration((short) seconds);
		return f;
	}
	
	private LibraryStatistics getStats() {
		browserDao.getJdbcTemplate().execute("select library.update_statistics()");
		return browserDao.getStatistics();
	}
	
	@Test
	public void findsTrackId() {
		String directory = "/root";
		String filename = "filename.flac";
		submitFile(additionDao, UnittestLibraryUtil.getFile(directory, filename));
		
		Assert.assertEquals(-1, browserDao.getTrackId(directory));
		Assert.assertEquals(-1, browserDao.getTrackId(filename));
		Assert.assertTrue(browserDao.getTrackId(directory + separatorChar + filename) != -1);
	}
	
	@Test
	public void findsVariousArtistsAlbums() {
		UnittestLibraryUtil.submitFile(additionDao, Arrays.asList(
				UnittestLibraryUtil.getFile("Various Artists", "VA 2", "title"),
				UnittestLibraryUtil.getFile("Various Artists", "VA 1", "title"),
				UnittestLibraryUtil.getFile("Artist", "Album", "title")));
		
		List<Album> albums = browserDao.getVariousArtistsAlbums();
		
		Assert.assertEquals(2, albums.size());
		Assert.assertEquals("VA 1", albums.get(0).getName());
		Assert.assertEquals("VA 2", albums.get(1).getName());

		Assert.assertNotNull(albums.get(0).getArtist());
		Assert.assertEquals(albums.get(0).getArtist(), albums.get(1).getArtist());
	}
	
}