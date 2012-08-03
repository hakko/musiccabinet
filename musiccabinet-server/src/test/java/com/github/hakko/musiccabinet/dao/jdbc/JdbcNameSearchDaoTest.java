package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.domain.model.aggr.NameSearchResult;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcNameSearchDaoTest {

	@Autowired
	private JdbcNameSearchDao searchDao;
	
	@Autowired
	private JdbcLibraryAdditionDao additionDao;
	
	@Before
	public void clearPreviousData() throws ApplicationException {
		searchDao.getJdbcTemplate().execute("truncate music.artist cascade");
		searchDao.getJdbcTemplate().execute("truncate library.file cascade");

		submitFile(additionDao, getFile("Casiotone For The Painfully Alone", "Young Shields", "Subway Home"));
		submitFile(additionDao, getFile("Casiotone For The Painfully Alone", "Etiquette", "New Year's Kiss"));
		submitFile(additionDao, getFile("Casiotone For The Painfully Alone", "Etiquette", "I Love Creedence"));
	}

	@Test
	public void findsArtists() {
		Assert.assertTrue(findsArtist("Casiotone for The Painfully Alone"));
		Assert.assertTrue(findsArtist("casioTONE for THE PAINfully ALONE"));
		Assert.assertTrue(findsArtist("Casiotone"));
		Assert.assertTrue(findsArtist("Casioton for painful"));
		
		Assert.assertFalse(findsArtist("Casiotone for The Awful Pains"));
		Assert.assertFalse(findsArtist("Cccazzziotn"));
	}

	private boolean findsArtist(String artistName) {
		NameSearchResult<Artist> artists = searchDao.getArtists(artistName, 0, 10);
		
		Assert.assertNotNull(artists);
		return artists.getTotalHits() == 1;
	}

	@Test
	public void findsAlbums() {
		NameSearchResult<Album> albums = searchDao.getAlbums("Casiotone", 0, 10);
		Assert.assertEquals(2, albums.getTotalHits());
		
		albums = searchDao.getAlbums("shield", 0, 10);
		Assert.assertEquals(1, albums.getTotalHits());

		albums = searchDao.getAlbums("etiquett", 0, 10);
		Assert.assertEquals(1, albums.getTotalHits());
	}

	@Test
	public void findsTracks() {
		NameSearchResult<Track> tracks = searchDao.getTracks("Casiotone", 0, 10);
		Assert.assertEquals(3, tracks.getTotalHits());
		
		tracks = searchDao.getTracks("casiotone etiquette", 0, 10);
		Assert.assertEquals(2, tracks.getTotalHits());

		tracks = searchDao.getTracks("young shield", 0, 10);
		Assert.assertEquals(1, tracks.getTotalHits());

	}

}