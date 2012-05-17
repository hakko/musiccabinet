package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_MUSICFILE;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcMusicFileDaoTest {

	/* Test data */
	private MusicFile mf1 = new MusicFile("THE BLACK HEART PROCESSION", "WE ALWAYS KNEW", 
			"/Users/hakko/Music/iTunes/iTunes Media/Music/The Black Heart Procession/Three/01 We Always Knew.mp3", 
			1322299535000L, 1322299535000L);
	private MusicFile mf2 = new MusicFile("THE BLACK HEART PROCESSION", "GUESS I'LL FORGET YOU", 
			"/Users/hakko/Music/iTunes/iTunes Media/Music/The Black Heart Procession/Three/02 Guess I'll Forget You.mp3", 
			1322299535000L, 1322299535000L);
	private MusicFile mf3 = new MusicFile("THE BLACK HEART PROCESSION", "ONCE SAID AT THE FIRES", 
			"/Users/hakko/Music/iTunes/iTunes Media/Music/The Black Heart Procession/Three/03 Once Said At The Fires.mp3",
			1322299536000L, 1322299536000L);
	private MusicFile mf4 = new MusicFile("16 HORSEPOWER", "CLOGGER", 
			"/Users/hakko/Music/iTunes/iTunes Media/Music/16 Horsepower/Clogger/01 Clogger.mp3",
			1298225089000L, 1298225089000L);
	
	@Autowired
	private JdbcMusicFileDao dao;

	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_MUSICFILE);
	}
	
	@Test
	public void createAndValidateMusicFiles() {
		deleteMusicFiles();
		
		List<MusicFile> musicFiles = Arrays.asList(mf1, mf2, mf3, mf4);
		
		createMusicFiles(musicFiles);
		List<MusicFile> storedMusicFiles = dao.getMusicFiles();

		assertNotNull(storedMusicFiles);
		for (MusicFile mf : storedMusicFiles) {
			assertTrue(musicFiles.contains(mf));
		}
	}

	@Test
	public void createSubsequentMusicFiles() {
		deleteMusicFiles();
		
		createMusicFiles(Arrays.asList(mf1, mf2));
		createMusicFiles(Arrays.asList(mf1, mf2, mf3));
		
		List<MusicFile> musicFiles = dao.getMusicFiles();
		assertEquals(musicFiles.size(), 3);
	}
	
	@Test
	public void createUpdatedVersionOfMusicFile() {
		deleteMusicFiles();
		
		createMusicFiles(Arrays.asList(mf1, mf2));

		mf1.setLastModified(mf1.getLastModified().plusDays(2));
		mf2.setLastModified(mf2.getLastModified().minusDays(2));
		createMusicFiles(Arrays.asList(mf1, mf2));

		List<MusicFile> musicFiles = dao.getMusicFiles();
		assertEquals(musicFiles.size(), 2);
		Assert.assertTrue(musicFiles.contains(mf1));
		Assert.assertTrue(musicFiles.contains(mf2));
	}
	
	@Test
	public void renamedTrackGetsRemovedBySecondImport() {
		deleteMusicFiles();
		
		createMusicFiles(asList(mf1, mf2));
		Assert.assertEquals(2, dao.getMusicFiles().size());
		
		createMusicFiles(asList(mf2, mf3));
		List<MusicFile> musicFiles = dao.getMusicFiles();
		Assert.assertEquals(2, musicFiles.size());
		Assert.assertFalse(musicFiles.contains(mf1));
		Assert.assertTrue(musicFiles.contains(mf2));
		Assert.assertTrue(musicFiles.contains(mf3));
	}

	@Test
	public void trackIdIsUniquePerTrack() throws ApplicationException {
		deleteMusicFiles();
		
		createMusicFiles(asList(mf1, mf2, mf3, mf4));
		
		int trackId1 = dao.getTrackId(mf1.getPath());
		int trackId2 = dao.getTrackId(mf2.getPath());
		int trackId3 = dao.getTrackId(mf3.getPath());
		int trackId4 = dao.getTrackId(mf4.getPath());
		
		Assert.assertTrue(trackId1 != trackId2);
		Assert.assertTrue(trackId2 != trackId3);
		Assert.assertTrue(trackId3 != trackId4);

		deleteMusicFiles();
		
		createMusicFiles(asList(mf1));

		int trackId1Again = dao.getTrackId(mf1.getPath());

		Assert.assertEquals(trackId1, trackId1Again);
	}
	
	@Test
	public void trackMatchesForCreatedMusicFile() {
		deleteMusicFiles();

		createMusicFiles(asList(mf1, mf2, mf3));
		
		Track track1 = dao.getTrack(mf1.getPath());
		Track track2 = dao.getTrack(mf2.getPath());
		Track track3 = dao.getTrack(mf3.getPath());

		Assert.assertTrue(mf1.getTrack().equals(track1));
		Assert.assertTrue(mf2.getTrack().equals(track2));
		Assert.assertTrue(mf3.getTrack().equals(track3));
	}

	private void deleteMusicFiles() {
		dao.getJdbcTemplate().execute("truncate library.musicfile cascade");
	}
	
	private void createMusicFiles(List<MusicFile> musicFiles) {
		dao.clearImport();
		dao.addMusicFiles(musicFiles);
		dao.createMusicFiles();
	}

}