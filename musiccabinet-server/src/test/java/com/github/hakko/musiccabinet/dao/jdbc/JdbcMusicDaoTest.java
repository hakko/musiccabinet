package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.GET_ARTIST_ID;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcMusicDaoTest {

	@Autowired
	private JdbcMusicDao dao;
	
	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, GET_ARTIST_ID);
	}

	@Test
	public void artistIdIsUniquePerArtist() {
		String madonna1 = "madonna", madonna2 = "madonna";
		String cher = "cher";
		
		int id1 = dao.getArtistId(madonna1);
		int id2 = dao.getArtistId(madonna2);
		int id3 = dao.getArtistId(cher);
		
		Assert.assertTrue(id1 == id2);
		Assert.assertFalse(id1 == id3);
	}
	
	@Test
	public void trackIdIsUniquePerTrack() {
		String artist1 = "MELISSA SWINGLE";
		String artist2 = "JOHN FAHEY";
		String artist3 = "ANI DIFRANCO";
		
		String trackName = "AMAZING GRACE";

		int trackId1 = dao.getTrackId(artist1, trackName);
		int trackId2 = dao.getTrackId(artist2, trackName);
		int trackId3 = dao.getTrackId(artist3, trackName);
			
		Assert.assertFalse(trackId1 == trackId2);
		Assert.assertFalse(trackId2 == trackId3);
		
		int trackId2Again = dao.getTrackId(artist2, trackName);
		
		Assert.assertEquals(trackId2, trackId2Again);
	}
	
	@Test
	public void trackIdCanBeUsedToFetchTrack() {
		final String artistName = "Death Grips";
		final String trackName = "I've Seen Footage";

		int trackId = dao.getTrackId(artistName, trackName);
		Track track = dao.getTrack(trackId);

		Assert.assertEquals(artistName, track.getArtist().getName());
		Assert.assertEquals(trackName, track.getName());
	}

}