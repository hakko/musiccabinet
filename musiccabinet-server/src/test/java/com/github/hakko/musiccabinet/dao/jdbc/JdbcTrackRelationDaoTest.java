package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_TRACKRELATION;
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
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.domain.model.music.TrackRelation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcTrackRelationDaoTest {

	/* Test data */
	private Track sourceTrack1 = new Track("Cher", "Believe");
	private TrackRelation tr1 = new TrackRelation(new Track("Cher", "Strong Enough"), 1f);
	private TrackRelation tr2 = new TrackRelation(new Track("Cher", "All Or Nothing"), 0.961879f);
	private TrackRelation tr3 = new TrackRelation(new Track("Madonna", "Vogue"), 0.291088f);
	private TrackRelation tr4 = new TrackRelation(new Track("Madonna", "Hung Up"), 0.286534f);

	private Track sourceTrack2 = new Track("Madonna", "Vogue");
	private TrackRelation tr5 = new TrackRelation(new Track("Madonna", "Express Yourself"), 1f);
	private TrackRelation tr6 = new TrackRelation(new Track("Kylie Minogue", "Get Outta My Way"), 0.275234f);
	private TrackRelation tr7 = new TrackRelation(new Track("Cher", "Believe"), 0.228768f);
	
	@Autowired
	private JdbcTrackRelationDao dao;

	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_TRACKRELATION);
	}
	
	@Test
	public void createAndValidateTrackRelations() {
		deleteTrackRelations();

		dao.createTrackRelations(sourceTrack1, Arrays.asList(tr1, tr2, tr3, tr4));
		dao.createTrackRelations(sourceTrack2, Arrays.asList(tr5, tr6, tr7));

		List<TrackRelation> cherRelations = dao.getTrackRelations(sourceTrack1);
		List<TrackRelation> madonnaRelations = dao.getTrackRelations(sourceTrack2);

		assertNotNull(cherRelations);
		assertNotNull(madonnaRelations);
		Assert.assertEquals(4, cherRelations.size());
		Assert.assertEquals(3, madonnaRelations.size());
		
		for (TrackRelation tr : Arrays.asList(tr1, tr2, tr3, tr4)) {
			assertTrue(cherRelations.contains(tr));
		}
		for (TrackRelation tr : Arrays.asList(tr5, tr6, tr7)) {
			assertTrue(madonnaRelations.contains(tr));
		}
	}

	@Test
	public void createAndValidateUpdatedTrackRelation() {
		deleteTrackRelations();

		dao.createTrackRelations(sourceTrack1, Arrays.asList(tr1, tr2, tr3, tr4));
		dao.createTrackRelations(sourceTrack2, Arrays.asList(tr5, tr6, tr7));

		tr3.setMatch(0.55f);
		tr7.setMatch(0.44f);

		dao.createTrackRelations(sourceTrack2, Arrays.asList(tr5, tr7));

		List<TrackRelation> cherRelations = dao.getTrackRelations(sourceTrack1);
		List<TrackRelation> madonnaRelations = dao.getTrackRelations(sourceTrack2);

		assertNotNull(cherRelations);
		assertNotNull(madonnaRelations);
		Assert.assertEquals(4, cherRelations.size());
		Assert.assertEquals(3, madonnaRelations.size());
		
		for (TrackRelation tr : Arrays.asList(tr1, tr2, tr4)) {
			assertTrue(cherRelations.contains(tr));
		}
		assertFalse(cherRelations.contains(tr3));
		for (TrackRelation tr : Arrays.asList(tr5, tr6, tr7)) {
			assertTrue(madonnaRelations.contains(tr));
		}
	}

	private void deleteTrackRelations() {
		dao.getJdbcTemplate().execute("truncate music.trackrelation cascade");
	}
	
}