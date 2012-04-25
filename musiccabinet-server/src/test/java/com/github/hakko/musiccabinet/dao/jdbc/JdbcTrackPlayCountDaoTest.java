package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_TRACKPLAYCOUNT_FROM_IMPORT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.TrackPlayCount;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcTrackPlayCountDaoTest {

	/* Test data */
	private TrackPlayCount tpc1 = new TrackPlayCount("THE BLACK HEART PROCESSION", 
			"WE ALWAYS KNEW", 14);
	private TrackPlayCount tpc2 = new TrackPlayCount("THE BLACK HEART PROCESSION", 
			"GUESS I'LL FORGET YOU", 12);
	private TrackPlayCount tpc3 = new TrackPlayCount("16 HORSEPOWER", 
			"CLOGGER", 7);
	
	@Autowired
	private JdbcTrackPlayCountDao dao;

	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_TRACKPLAYCOUNT_FROM_IMPORT);
	}
	
	@Test
	public void createAndValidateTrackPlayCounts() {
		deleteTrackPlayCounts();
		
		List<TrackPlayCount> trackPlayCounts = Arrays.asList(tpc1, tpc2, tpc3);
		
		dao.createTrackPlayCounts(trackPlayCounts);
		List<TrackPlayCount> storedTrackPlayCounts = dao.getTrackPlayCounts();

		assertNotNull(storedTrackPlayCounts);
		for (TrackPlayCount tpc : storedTrackPlayCounts) {
			assertTrue(trackPlayCounts.contains(tpc));
		}
	}

	@Test
	public void createSubsequentTrackPlayCount() {
		deleteTrackPlayCounts();
		
		dao.createTrackPlayCounts(Arrays.asList(tpc1, tpc2));
		dao.createTrackPlayCounts(Arrays.asList(tpc2, tpc3));
		List<TrackPlayCount> trackPlayCounts = dao.getTrackPlayCounts();
		assertEquals(trackPlayCounts.size(), 3);
	}
	
	@Test
	public void createUpdatedVersionOfTrackPlayCount() {
		deleteTrackPlayCounts();
		
		dao.createTrackPlayCounts(Arrays.asList(tpc1, tpc2));

		tpc1.setPlayCount(tpc1.getPlayCount() - 2);
		tpc2.setPlayCount(tpc2.getPlayCount() + 2);
		
		dao.createTrackPlayCounts(Arrays.asList(tpc1, tpc2));

		List<TrackPlayCount> trackPlayCounts = dao.getTrackPlayCounts();
		assertEquals(trackPlayCounts.size(), 2);
		for (TrackPlayCount tpc : trackPlayCounts) {
			if (tpc.getTrack().equals(tpc1.getTrack())) {
				assertTrue(tpc.getPlayCount() != tpc1.getPlayCount());
			} else if (tpc.getTrack().equals(tpc2.getTrack())) {
				assertTrue(tpc.getPlayCount() == tpc2.getPlayCount());
			} else {
				assertTrue("Either file1 or 2 should have been returned.", false);
			}
		}
	}

	private void deleteTrackPlayCounts() {
		dao.getJdbcTemplate().execute("truncate library.trackplaycount cascade");
	}
	
}