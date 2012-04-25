package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_ALBUMINFO_FROM_IMPORT;
import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_MUSICDIRECTORY_FROM_IMPORT;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.MusicDirectory;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.AlbumInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.AlbumInfoParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcAlbumInfoDaoTest {

	/* Test data */
	private AlbumInfo aiNirvana, aiNirvana2, aiNirvana3, aiHurts, aiSchuller;

	private static final String AI_NIRVANA_FILE = 
			"last.fm/xml/albuminfo.nirvana.nevermind.xml";
	private static final String AI_NIRVANA2_FILE = 
			"last.fm/xml/albuminfo.nirvana.bleach.xml";
	private static final String AI_NIRVANA3_FILE = 
			"last.fm/xml/albuminfo.nirvana.incesticide.xml";
	private static final String AI_HURTS_FILE = 
			"last.fm/xml/albuminfo.hurts.happiness.xml";
	private static final String AI_SCHULLER_FILE = 
			"last.fm/xml/albuminfo.sebastienschuller.happiness.xml";
	
	@Autowired
	private JdbcAlbumInfoDao dao;
	
	@Autowired
	private JdbcMusicDirectoryDao musicDirectoryDao;

	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_ALBUMINFO_FROM_IMPORT);
		PostgreSQLUtil.loadFunction(dao, UPDATE_MUSICDIRECTORY_FROM_IMPORT);
		
		aiNirvana = new AlbumInfoParserImpl(new ResourceUtil(
				AI_NIRVANA_FILE).getInputStream()).getAlbumInfo();
		aiHurts = new AlbumInfoParserImpl(new ResourceUtil(
				AI_HURTS_FILE).getInputStream()).getAlbumInfo();
		aiSchuller = new AlbumInfoParserImpl(new ResourceUtil(
				AI_SCHULLER_FILE).getInputStream()).getAlbumInfo();
		aiNirvana2 = new AlbumInfoParserImpl(new ResourceUtil(
				AI_NIRVANA2_FILE).getInputStream()).getAlbumInfo();
		aiNirvana3 = new AlbumInfoParserImpl(new ResourceUtil(
				AI_NIRVANA3_FILE).getInputStream()).getAlbumInfo();
		
		deleteArtists();

		createMusicDirectories(aiNirvana, aiNirvana2, aiNirvana3, aiHurts, aiSchuller);
	}
	
	@Test
	public void createAndValidateArtistInfos() throws ApplicationException {
		deleteAlbumInfos();

		List<AlbumInfo> albumInfos = new ArrayList<AlbumInfo>();
		albumInfos.add(aiNirvana);
		albumInfos.add(aiHurts);
		
		dao.createAlbumInfo(albumInfos);
		
		AlbumInfo dbNirvana = dao.getAlbumInfo(aiNirvana.getAlbum());
		AlbumInfo dbHurts = dao.getAlbumInfo(aiHurts.getAlbum());
		
		Assert.assertEquals(aiNirvana, dbNirvana);
		Assert.assertEquals(aiHurts, dbHurts);
	}

	@Test
	public void createAndValidateUpdatedArtistInfos() throws ApplicationException {
		deleteAlbumInfos();

		String newSmallUrl = "http://userserve-ak.last.fm/serve/34s/1234567.png";
		int newListeners = 1234567;
		
		dao.createAlbumInfo(Arrays.asList(aiNirvana, aiSchuller));
		
		aiNirvana.setSmallImageUrl(newSmallUrl);
		aiNirvana.setListeners(newListeners);
		
		dao.createAlbumInfo(Arrays.asList(aiNirvana, aiSchuller));
		
		AlbumInfo dbNirvana = dao.getAlbumInfo(aiNirvana.getAlbum());
		
		Assert.assertEquals(newSmallUrl, dbNirvana.getSmallImageUrl());
		Assert.assertEquals(newListeners, dbNirvana.getListeners());
	}
	
	@Test
	public void allAlbumsWithoutInfoAreReturned() {
		deleteAlbumInfos();
		deleteMusicDirectories();
		createMusicDirectories(aiNirvana, aiHurts, aiSchuller);
		
		List<Album> albums = dao.getAlbumsWithoutInfo();
		Assert.assertNotNull(albums);
		Assert.assertTrue(albums.contains(aiNirvana.getAlbum()));
		Assert.assertTrue(albums.contains(aiHurts.getAlbum()));
		Assert.assertTrue(albums.contains(aiSchuller.getAlbum()));

		for (Album album : albums) {
			Assert.assertNotNull(album.getName());
			Assert.assertNotNull(album.getArtist().getName());
		}
	}
	
	@Test
	public void handlesAlbumWithIdenticalTitleByDifferentArtists() {
		deleteAlbumInfos();
		deleteMusicDirectories();
		createMusicDirectories(aiHurts, aiSchuller);
		dao.createAlbumInfo(Arrays.asList(aiHurts, aiSchuller));
		
		for (AlbumInfo ai : Arrays.asList(aiHurts, aiSchuller)) {
			List<AlbumInfo> dbInfos = dao.getAlbumInfosForArtist(ai.getAlbum().getArtist());
			Assert.assertNotNull(dbInfos);
			Assert.assertEquals(1, dbInfos.size());
			AlbumInfo dbInfo = dbInfos.get(0);
			Assert.assertEquals(ai.getAlbum().getName(), dbInfo.getAlbum().getName());
			Assert.assertEquals(ai.getAlbum().getArtist(), dbInfo.getAlbum().getArtist());
		}
	}
	
	@Test
	public void handlesMultipleAlbumsBySameArtist() {
		deleteAlbumInfos();
		deleteMusicDirectories();
		createMusicDirectories(aiNirvana, aiNirvana2, aiNirvana3);
		dao.createAlbumInfo(Arrays.asList(aiNirvana, aiNirvana2, aiNirvana3));
		
		List<AlbumInfo> dbInfos = dao.getAlbumInfosForArtist(aiNirvana.getAlbum().getArtist());
		assertNotNull(dbInfos);
		assertEquals(3, dbInfos.size());

		Set<String> dbAlbumNames = new HashSet<String>();
		for (AlbumInfo dbInfo : dbInfos) {
			assertEquals(dbInfo.getAlbum().getArtist(), aiNirvana.getAlbum().getArtist());
			dbAlbumNames.add(dbInfo.getAlbum().getName());
		}
		
		for (AlbumInfo ai : Arrays.asList(aiNirvana, aiNirvana2, aiNirvana3)) {
			Assert.assertTrue(dbAlbumNames.contains(ai.getAlbum().getName()));
		}
	}
	
	@Test
	public void mapsPathToAlbumInfo() {
		deleteAlbumInfos();
		deleteMusicDirectories();
		List<MusicDirectory> dirs = createMusicDirectories(aiNirvana);
		dao.createAlbumInfo(Arrays.asList(aiNirvana));
		
		Assert.assertEquals(1, dirs.size());
		String path = dirs.get(0).getPath();
		
		Map<String, AlbumInfo> albumInfos = dao.getAlbumInfosForPaths(asList(path));
		
		Assert.assertEquals(1, albumInfos.size());
		Assert.assertTrue(albumInfos.containsKey(path));
		Assert.assertNotNull(albumInfos.get(path));
		Assert.assertEquals(aiNirvana.getAlbum().getName(),
				albumInfos.get(path).getAlbum().getName());
	}

	@Test
	public void mapsPathsToAlbumInfos() {
		deleteAlbumInfos();
		deleteMusicDirectories();
		List<MusicDirectory> dirs = createMusicDirectories(aiNirvana, aiSchuller, aiNirvana2, aiHurts);
		dao.createAlbumInfo(Arrays.asList(aiNirvana, aiSchuller, aiNirvana2, aiHurts));
		
		Assert.assertEquals(4, dirs.size());
		Map<String, String> pathToAlbumName = new HashMap<String, String>();
		for (MusicDirectory dir : dirs) {
			pathToAlbumName.put(dir.getPath(), dir.getAlbumName());
		}

		Map<String, AlbumInfo> albumInfos = dao.getAlbumInfosForPaths(
				new ArrayList<String>(pathToAlbumName.keySet()));

		for (String path : pathToAlbumName.keySet()) {
			Assert.assertTrue(albumInfos.containsKey(path));
			Assert.assertEquals(pathToAlbumName.get(path), 
					albumInfos.get(path).getAlbum().getName());
		}
	}

	@Test
	public void onlyPresentArtistsWithoutInfoAreReturned() {
		deleteAlbumInfos();
		deleteMusicDirectories();
		createMusicDirectories(aiSchuller);

		List<Album> albums = dao.getAlbumsWithoutInfo();
		Assert.assertNotNull(albums);
		Assert.assertTrue(albums.contains(aiSchuller.getAlbum()));
		Assert.assertFalse(albums.contains(aiNirvana.getAlbum()));
		Assert.assertFalse(albums.contains(aiHurts.getAlbum()));
	}
	
	private void deleteArtists() {
		dao.getJdbcTemplate().execute("truncate music.artist cascade");
	}
	
	private void deleteAlbumInfos() {
		dao.getJdbcTemplate().execute("truncate music.albuminfo cascade");
	}
	
	private void deleteMusicDirectories() {
		dao.getJdbcTemplate().execute("truncate library.musicdirectory cascade");
	}

	private List<MusicDirectory> createMusicDirectories(AlbumInfo... albumInfos) {
		List<MusicDirectory> musicDirectories = new ArrayList<MusicDirectory>();
		for (AlbumInfo ai : albumInfos) {
			String artistName = ai.getAlbum().getArtist().getName();
			String albumName = ai.getAlbum().getName();
			musicDirectories.add(new MusicDirectory(artistName, albumName,
					"/path/to/" + artistName + "/" + albumName));
		}
		musicDirectoryDao.clearImport();
		musicDirectoryDao.addMusicDirectories(musicDirectories);
		musicDirectoryDao.createMusicDirectories();
		return musicDirectories;
	}
	
}