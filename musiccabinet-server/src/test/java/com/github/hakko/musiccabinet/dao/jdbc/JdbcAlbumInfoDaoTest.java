package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_ALBUMINFO;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.AlbumInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.AlbumInfoParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

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
	private MusicDao musicDao;
	
	@Autowired
	private JdbcLibraryAdditionDao libraryAdditionDao;
	
	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_ALBUMINFO);
		
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
		deleteLibraryTracks();
		createLibraryTracks(aiNirvana, aiNirvana2, aiNirvana3, aiHurts, aiSchuller);
		
		for (AlbumInfo ai : asList(aiNirvana, aiHurts, aiSchuller)) {
			ai.getAlbum().getArtist().setId(
					musicDao.getArtistId(ai.getAlbum().getArtist()));
		}
	}
	
	@Test
	public void createAndValidateArtistInfos() throws ApplicationException {
		deleteAlbumInfos();

		List<AlbumInfo> albumInfos = new ArrayList<>();
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
		deleteLibraryTracks();
		createLibraryTracks(aiNirvana, aiHurts, aiSchuller);
		
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
		deleteLibraryTracks();
		createLibraryTracks(aiHurts, aiSchuller);
		dao.createAlbumInfo(Arrays.asList(aiHurts, aiSchuller));
		
		for (AlbumInfo ai : Arrays.asList(aiHurts, aiSchuller)) {
			List<AlbumInfo> dbInfos = dao.getAlbumInfosForArtist(
					ai.getAlbum().getArtist().getId());
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
		deleteLibraryTracks();
		createLibraryTracks(aiNirvana, aiNirvana2, aiNirvana3);
		dao.createAlbumInfo(Arrays.asList(aiNirvana, aiNirvana2, aiNirvana3));
		
		List<AlbumInfo> dbInfos = dao.getAlbumInfosForArtist(
				aiNirvana.getAlbum().getArtist().getId());
		assertNotNull(dbInfos);
		assertEquals(3, dbInfos.size());

		Set<String> dbAlbumNames = new HashSet<>();
		for (AlbumInfo dbInfo : dbInfos) {
			assertEquals(dbInfo.getAlbum().getArtist(), aiNirvana.getAlbum().getArtist());
			dbAlbumNames.add(dbInfo.getAlbum().getName());
		}
		
		for (AlbumInfo ai : Arrays.asList(aiNirvana, aiNirvana2, aiNirvana3)) {
			Assert.assertTrue(dbAlbumNames.contains(ai.getAlbum().getName()));
		}
	}

	@Test
	public void onlyPresentArtistsWithoutInfoAreReturned() {
		deleteAlbumInfos();
		deleteLibraryTracks();
		createLibraryTracks(aiSchuller);

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
	
	private void createLibraryTracks(AlbumInfo... albumInfos) {
		List<File> files = new ArrayList<>();
		for (AlbumInfo ai : albumInfos) {
			String artistName = ai.getAlbum().getArtist().getName();
			String albumName = ai.getAlbum().getName();
			files.add(getFile(artistName, albumName, null));
		}
		UnittestLibraryUtil.submitFile(libraryAdditionDao, files);
	}
	
	private void deleteLibraryTracks() {
		libraryAdditionDao.getJdbcTemplate().execute("truncate library.file cascade");
	}

}