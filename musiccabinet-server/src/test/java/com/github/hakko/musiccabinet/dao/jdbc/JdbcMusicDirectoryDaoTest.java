package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_MUSICDIRECTORY;

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
import com.github.hakko.musiccabinet.domain.model.library.MusicDirectory;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcMusicDirectoryDaoTest {

	/* Test data */
	private MusicDirectory mdArtist = new MusicDirectory("Lykke Li", "/Users/hakko/Music/iTunes/iTunes Media/Music/Lykke Li");
	private MusicDirectory mdAlbum = new MusicDirectory("Lykke Li", "After laughter (comes tears)", "/Users/hakko/Music/iTunes/iTunes Media/Music/Lykke Li/After Laughter (Comes Tears)");
	
	@Autowired
	private JdbcMusicDirectoryDao dao;
	
	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_MUSICDIRECTORY);
	}
	
	@Test
	public void artistDirectoryAndAlbumDirectoryCanBeStoredAndFetched() {
		deleteMusicDirectories();
		
		dao.clearImport();
		dao.addMusicDirectories(Arrays.asList(mdArtist, mdAlbum));
		dao.createMusicDirectories();
		
		List<MusicDirectory> musicDirectories = dao.getMusicDirectories();

		Assert.assertTrue(musicDirectories.contains(mdArtist));
		Assert.assertTrue(musicDirectories.contains(mdAlbum));
	}

	@Test
	public void artistDirectoryAndAlbumDirectoryHasSameArtistId() throws ApplicationException {
		deleteMusicDirectories();
		
		dao.clearImport();
		dao.addMusicDirectories(Arrays.asList(mdArtist, mdAlbum));
		dao.createMusicDirectories();
		
		int artistId = dao.getArtistId(mdArtist.getPath());
		int albumArtistId = dao.getArtistId(mdAlbum.getPath());
		
		Assert.assertEquals(artistId, albumArtistId);
	}
	
	@Test
	public void oldMusicDirectoriesGetsRemoved() {
		deleteMusicDirectories();
		
		dao.clearImport();
		dao.addMusicDirectories(Arrays.asList(mdArtist));
		dao.createMusicDirectories();
		List<MusicDirectory> musicDirectories = dao.getMusicDirectories();
		Assert.assertTrue(musicDirectories.contains(mdArtist));
		Assert.assertFalse(musicDirectories.contains(mdAlbum));
		
		dao.clearImport();
		dao.addMusicDirectories(Arrays.asList(mdAlbum));
		dao.createMusicDirectories();
		musicDirectories = dao.getMusicDirectories();
		Assert.assertFalse(musicDirectories.contains(mdArtist));
		Assert.assertTrue(musicDirectories.contains(mdAlbum));
	}

	@Test
	public void oldMusicDirectoriesGetsUpdated() {
		deleteMusicDirectories();
		
		dao.clearImport();
		dao.addMusicDirectories(Arrays.asList(mdAlbum));
		dao.createMusicDirectories();
		
		MusicDirectory mdOverwrittenAlbum = new MusicDirectory("xx", mdAlbum.getPath());
		
		dao.clearImport();
		dao.addMusicDirectories(Arrays.asList(mdOverwrittenAlbum));
		dao.createMusicDirectories();
		List<MusicDirectory> musicDirectories = dao.getMusicDirectories();
		Assert.assertEquals(1, musicDirectories.size());
		Assert.assertTrue(musicDirectories.contains(mdOverwrittenAlbum));
	}
	
	@Test
	public void artistIdIsUniquePerArtist() throws ApplicationException {
		deleteMusicDirectories();
		
		Artist garbochock = new Artist("Garbochock");
		Artist garyNuman = new Artist("Gary Numan");

		MusicDirectory md1 = new MusicDirectory(garbochock.getName(), 
		"/Users/hakko/Music/iTunes/iTunes Media/Music/Garbochock");
		MusicDirectory md2 = new MusicDirectory(garbochock.getName(), "Ritual",
		"/Users/hakko/Music/iTunes/iTunes Media/Music/Garbochock/Ritual");

		MusicDirectory md3 = new MusicDirectory(garyNuman.getName(), 
		"/Users/hakko/Music/iTunes/iTunes Media/Music/Gary Numan");
		MusicDirectory md4 = new MusicDirectory(garyNuman.getName(), "Exile",
		"/Users/hakko/Music/iTunes/iTunes Media/Music/Gary Numan/Exile");
		
		dao.clearImport();
		dao.addMusicDirectories(Arrays.asList(md1, md2, md3, md4));
		dao.createMusicDirectories();
		
		int artistId1 = dao.getArtistId(md1.getPath());
		int artistId2 = dao.getArtistId(md2.getPath());
		int artistId3 = dao.getArtistId(md3.getPath());
		int artistId4 = dao.getArtistId(md4.getPath());
		
		Assert.assertTrue(artistId1 == artistId2);
		Assert.assertTrue(artistId2 != artistId3);
		Assert.assertTrue(artistId3 == artistId4);

		deleteMusicDirectories();
		
		dao.clearImport();
		dao.addMusicDirectories(Arrays.asList(md1));
		dao.createMusicDirectories();

		int artistId1Again = dao.getArtistId(md1.getPath());
		
		Assert.assertEquals(artistId1, artistId1Again);
	}

	private void deleteMusicDirectories() {
		dao.getJdbcTemplate().execute("truncate library.musicdirectory cascade");
	}

}