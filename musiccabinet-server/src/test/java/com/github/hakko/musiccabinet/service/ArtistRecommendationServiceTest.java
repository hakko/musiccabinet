package com.github.hakko.musiccabinet.service;

import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.LibraryAdditionDao;
import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

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
	private LibraryAdditionDao additionDao;

	@Autowired
	private MusicDao musicDao;
	
	private int madonnaId;
	
	@Before
	public void createArtistToSearchFor() {
		File file = getFile("Madonna", null, null);
		UnittestLibraryUtil.submitFile(additionDao, file);
		
		madonnaId = musicDao.getArtistId("Madonna");
	}
	
	@Test
	public void serviceConfigured() {
		Assert.assertNotNull(arService);
		Assert.assertNotNull(arService.artistRecommendationDao);
	}

	@Test
	public void canInvokeRelatedArtistsInLibrary() throws ApplicationException {
		arService.getRelatedArtistsInLibrary(madonnaId, 20);
	}

	@Test
	public void canInvokeRelatedArtistsNotInLibrary() throws ApplicationException {
		arService.getRelatedArtistsNotInLibrary(madonnaId, 20);
	}
	
	@Test
	public void canInvokeMatchingSongs() throws ApplicationException {
		arService.getMatchingSongs(madonnaId);
	}
	
	@Test
	public void canInvokeRecommendedArtistsFromGenre() {
		arService.getRecommendedArtistsFromGenre("outlaw country", 0, 20);
	}

}