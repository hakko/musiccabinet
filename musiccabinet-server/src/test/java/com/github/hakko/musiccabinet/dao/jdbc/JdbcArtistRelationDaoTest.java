package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_ARTISTRELATION;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistRelation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcArtistRelationDaoTest {

	/* Test data */
	private Artist sourceArtist1 = new Artist("Cher");
	private ArtistRelation ar1 = new ArtistRelation(new Artist("Céline Dion"), 0.406959f);
	private ArtistRelation ar2 = new ArtistRelation(new Artist("Kylie Minogue"), 0.364589f);
	private ArtistRelation ar3 = new ArtistRelation(new Artist("Tina Turner"), 0.347697f);
	private ArtistRelation ar4 = new ArtistRelation(new Artist("Barbra Streisand"), 0.313871f);
	
	private Artist sourceArtist2 = new Artist("Kylie Minogue");
	private ArtistRelation ar5 = new ArtistRelation(new Artist("Geri Halliwell"), 0.291044f);
	private ArtistRelation ar6 = new ArtistRelation(new Artist("Jennifer Lopez"), 0.208765f);
	private ArtistRelation ar7 = new ArtistRelation(new Artist("Cher"), 0.195111f);
	
	@Autowired
	private JdbcArtistRelationDao dao;

	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_ARTISTRELATION);
		
		PostgreSQLUtil.truncateTables(dao);
	}
	
	@Test
	public void createAndValidateArtistRelations() {
		deleteArtistRelations();

		dao.createArtistRelations(sourceArtist1, Arrays.asList(ar1, ar2, ar3, ar4));
		dao.createArtistRelations(sourceArtist2, Arrays.asList(ar5, ar6, ar7));

		List<ArtistRelation> cherRelations = dao.getArtistRelations(sourceArtist1);
		List<ArtistRelation> kylieRelations = dao.getArtistRelations(sourceArtist2);

		assertNotNull(cherRelations);
		assertNotNull(kylieRelations);
		Assert.assertEquals(4, cherRelations.size());
		Assert.assertEquals(3, kylieRelations.size());
		
		for (ArtistRelation ar : Arrays.asList(ar1, ar2, ar3, ar4)) {
			assertTrue(cherRelations.contains(ar));
		}
		for (ArtistRelation ar : Arrays.asList(ar5, ar6, ar7)) {
			assertTrue(kylieRelations.contains(ar));
		}
		
	}

	@Test
	public void createAndValidateUpdatedArtistRelation() {
		deleteArtistRelations();

		dao.createArtistRelations(sourceArtist1, Arrays.asList(ar1, ar2, ar3, ar4));
		dao.createArtistRelations(sourceArtist2, Arrays.asList(ar5, ar6, ar7));

		ar3.setMatch(0.55f);
		ar7.setMatch(0.44f);

		dao.createArtistRelations(sourceArtist2, Arrays.asList(ar5, ar7));

		List<ArtistRelation> cherRelations = dao.getArtistRelations(sourceArtist1);
		List<ArtistRelation> kylieRelations = dao.getArtistRelations(sourceArtist2);

		assertNotNull(cherRelations);
		assertNotNull(kylieRelations);
		Assert.assertEquals(4, cherRelations.size());
		Assert.assertEquals(3, kylieRelations.size());
		
		for (ArtistRelation ar : Arrays.asList(ar1, ar2, ar4)) {
			assertTrue(cherRelations.contains(ar));
		}
		assertFalse(cherRelations.contains(ar3));
		for (ArtistRelation ar : Arrays.asList(ar5, ar6, ar7)) {
			assertTrue(kylieRelations.contains(ar));
		}
	}
		
	private void deleteArtistRelations() {
		dao.getJdbcTemplate().execute("truncate music.artistrelation cascade");
	}
	
}