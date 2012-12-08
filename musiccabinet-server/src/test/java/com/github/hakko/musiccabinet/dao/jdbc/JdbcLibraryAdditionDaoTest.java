package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.set;
import static java.lang.String.format;
import static org.joda.time.DateTime.parse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.domain.model.library.MetaData.Mediatype;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcLibraryAdditionDaoTest {
	
	@Autowired
	private JdbcLibraryAdditionDao libraryAdditionDao;

	@Autowired
	private JdbcLibraryBrowserDao libraryBrowserDao;

	@Before
	public void createAlbumTestData() throws ApplicationException {
		PostgreSQLUtil.truncateTables(libraryAdditionDao);

		final String DIR = "/dir", FILE = "file";
		int fileNr = 0;

		List<File> files = new ArrayList<>();
		for (short year : new short[]{2000, 2002, 2004, 2001, 2003}) {
			File file = new File(DIR, FILE + ++fileNr, parse(format("%d-01-01", year)), 0);
			MetaData md = new MetaData();
			md.setArtist("artist");
			md.setTitle("title " + fileNr);
			md.setAlbum("album " + fileNr);
			md.setYear(year);
			md.setMediaType(Mediatype.OGG);
			file.setMetaData(md);
			files.add(file);
		}
		
		libraryAdditionDao.addSubdirectories(null, set(DIR));
		libraryAdditionDao.addFiles(DIR, new HashSet<>(files));
		libraryAdditionDao.updateLibrary();
	}
	
	@Test
	public void addsAlbumsOrderedByFileModificationDate() {
		List<Album> albums = libraryBrowserDao.getRecentlyAddedAlbums(0, 10, null);
		
		Assert.assertEquals(5, albums.size());
		
		Assert.assertEquals(2004, albums.get(0).getYear());
		Assert.assertEquals(2003, albums.get(1).getYear());
		Assert.assertEquals(2002, albums.get(2).getYear());
		Assert.assertEquals(2001, albums.get(3).getYear());
		Assert.assertEquals(2000, albums.get(4).getYear());
	}
		
}