package com.github.hakko.musiccabinet.service;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcMusicDao;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.ResourceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class SubsonicIndexServiceTest {

	@Autowired
	private SubsonicIndexService libraryIndexService;

	@Autowired
	private JdbcMusicDao musicDao;

	private static final String LIBRARY_INDEX_FILE = 
		"subsonic/subsonic15.index";
	
	@Test
	public void libraryIndexUpdateServiceConfigured() throws ApplicationException {
		Assert.assertNotNull(libraryIndexService);
		Assert.assertNotNull(libraryIndexService.musicFileDao);
		Assert.assertNotNull(libraryIndexService.musicDirectoryDao);
	}
	
	@Test
	public void allArtistsHaveNameCapitalizations() throws ApplicationException {
		libraryIndexService.updateLibraryIndex(
				new ResourceUtil(LIBRARY_INDEX_FILE).getInputStream());
		
		for (String artistName : Arrays.asList("BIG K.R.I.T.", "GOD IS AN ASTRONAUT",
				"SONGS: OHIA", "SUSANNE SUNDFØR", "SÄKERT!", "ÓLAFUR ARNALDS")) {
			String dbArtistName = musicDao.getArtist(artistName).getName();
			
			Assert.assertNotNull(dbArtistName);
			Assert.assertFalse(dbArtistName.equals(artistName));
			Assert.assertTrue(dbArtistName.equalsIgnoreCase(artistName));
		}
	}
	
	@Test (expected = DataAccessException.class)
	public void artistNotInImportLacksCapitalization() {
		Artist artist = new Artist("ARTIST NOT IN IMPORT");
		
		musicDao.getArtist(artist.getName());
		Assert.fail();
	}
	
}