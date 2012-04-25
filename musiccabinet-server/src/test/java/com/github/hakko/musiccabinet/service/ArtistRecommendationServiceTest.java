package com.github.hakko.musiccabinet.service;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.MusicDirectoryDao;
import com.github.hakko.musiccabinet.domain.model.library.MusicDirectory;
import com.github.hakko.musiccabinet.exception.ApplicationException;

/*
 * Doesn't actually do much testing, except invoking the service methods.
 * 
 * Detailed test are found in JdbcArtistRecommendationDaoTest.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class ArtistRecommendationServiceTest {

	@Autowired
	private ArtistRecommendationService arService;
	
	@Autowired
	private MusicDirectoryDao musicDirectoryDao;

	private MusicDirectory madonnaDir = new MusicDirectory("Madonna", null, "/path/to/madonna");
	
	@Before
	public void createArtistToSearchFor() {
		musicDirectoryDao.clearImport();
		musicDirectoryDao.addMusicDirectories(Arrays.asList(madonnaDir));
		musicDirectoryDao.createMusicDirectories();
	}
	
	@Test
	public void serviceConfigured() {
		Assert.assertNotNull(arService);
		Assert.assertNotNull(arService.artistRecommendationDao);
		Assert.assertNotNull(arService.musicDirectoryDao);
	}

	@Test
	public void canInvokeRelatedArtistsInLibrary() throws ApplicationException {
		arService.getRelatedArtistsInLibrary(madonnaDir.getPath(), 20);
	}

	@Test
	public void canInvokeRelatedArtistsNotInLibrary() throws ApplicationException {
		arService.getRelatedArtistsNotInLibrary(madonnaDir.getPath(), 20);
	}
	
	@Test
	public void canInvokeMatchingSongs() throws ApplicationException {
		arService.getMatchingSongs(madonnaDir.getPath());
	}
	
	@Test
	public void canInvokeRecommendedArtistsFromGenre() {
		arService.getRecommendedArtistsFromGenre("outlaw country", 0, 20);
	}

}