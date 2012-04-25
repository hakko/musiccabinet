package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_ARTISTTOPTAG_FROM_IMPORT;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.dao.MusicFileDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTagsParser;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTagsParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcArtistTopTagsDaoTest {

	@Autowired
	private MusicDao musicDao;
	
	@Autowired
	private MusicFileDao musicFileDao;

	@Autowired
	private JdbcArtistTopTagsDao dao;

	// testdata
	private Artist cherArtist;
	private Artist rihannaArtist;
	private List<Tag> cherTopTags;
	private List<Tag> rihannaTopTags;
	
	private static final String CHER_TOP_TAGS = "last.fm/xml/toptags.cher.xml";
	private static final String RIHANNA_TOP_TAGS = "last.fm/xml/toptags.rihanna.xml";

	@Before
	public void loadFunctionDependencyAndTestdata() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_ARTISTTOPTAG_FROM_IMPORT);

		ArtistTopTagsParser cherParser = new ArtistTopTagsParserImpl(
				new ResourceUtil(CHER_TOP_TAGS).getInputStream());
		ArtistTopTagsParser rihannaParser = new ArtistTopTagsParserImpl(
				new ResourceUtil(RIHANNA_TOP_TAGS).getInputStream());
		cherArtist = cherParser.getArtist();
		cherTopTags = cherParser.getTopTags();
		rihannaArtist = rihannaParser.getArtist();
		rihannaTopTags = rihannaParser.getTopTags();

		PostgreSQLUtil.truncateTables(dao);
	}

	@Test
	public void topTagsAreEmptyAfterClearing() throws ApplicationException {
		deleteArtistTopTags();
	
		List<Tag> cherStoredTracks = dao.getTopTags(cherArtist);
		assertNotNull(cherStoredTracks);
		assertEquals(0, cherStoredTracks.size());
	}

	@Test
	public void storeAndValidateTopTags() throws ApplicationException {
		deleteArtistTopTags();
		
		dao.createTopTags(cherArtist, cherTopTags);
		List<Tag> cherStoredTags = dao.getTopTags(cherArtist);
		assertEquals(cherTopTags.size(), cherStoredTags.size());
		for (int i = 0; i < cherTopTags.size(); i++) {
			assertTrue(cherStoredTags.contains(cherTopTags.get(i)));
		}
		
		List<Tag> rihannaStoredTags = dao.getTopTags(rihannaArtist);
		assertEquals(0, rihannaStoredTags.size());
	}

	@Test
	public void storeUpdateAndValidateTopTags() throws ApplicationException {
		deleteArtistTopTags();
		
		dao.createTopTags(cherArtist, cherTopTags);
		dao.createTopTags(rihannaArtist, rihannaTopTags);
		
		rihannaTopTags = new ArrayList<Tag>();
		rihannaTopTags.add(new Tag("dance", (short) 22));
		rihannaTopTags.add(new Tag("r&b", (short) 8));
		dao.createTopTags(rihannaArtist, rihannaTopTags);
		
		List<Tag> cherStoredTopTags = dao.getTopTags(cherArtist);
		List<Tag> rihannaStoredTopTags = dao.getTopTags(rihannaArtist);
		
		assertEquals(100, cherStoredTopTags.size());
		assertEquals(2, rihannaStoredTopTags.size());

		for (int i = 0; i < cherTopTags.size(); i++) {
			assertTrue(cherStoredTopTags.contains(cherTopTags.get(i)));
		}
		for (int i = 0; i < rihannaTopTags.size(); i++) {
			assertTrue(rihannaStoredTopTags.contains(rihannaTopTags.get(i)));
		}
	}
	
	@Test
	public void noArtistsMeanNoMissingTopTags() {
		deleteArtists();
		
		List<Artist> artists = dao.getArtistsWithoutTopTags();
		Assert.assertNotNull(artists);
		Assert.assertEquals(0, artists.size());
	}
	
	@Test
	public void oneArtistMeansOneMissingTopTags() {
		final String artistName = "Piano Magic";
		final String trackName = "Kingfisher / Grass";
		final String path = "/";
		final long lastModified = System.currentTimeMillis(), created = lastModified;
		
		deleteArtists();
		
		createMusicFiles(Arrays.asList(
				new MusicFile(artistName, trackName, path, created, lastModified)));
		
		List<Artist> artists = dao.getArtistsWithoutTopTags();
		Assert.assertNotNull(artists);
		Assert.assertEquals(1, artists.size());
		Assert.assertTrue(artistName.equals(artists.get(0).getName()));
	}

	@Test
	public void onlyArtistsWithoutTopTracksAreReturned() {
		long time = System.currentTimeMillis();
		MusicFile mf1 = new MusicFile("Emily Barker", "Blackbird", "/path1", time, time);
		MusicFile mf2 = new MusicFile("Emily Haines", "Our Hell", "/path2", time, time);
		MusicFile mf3 = new MusicFile("Emily Jane White", "Dagger", "/path3", time, time);
		
		Artist artist1 = mf1.getTrack().getArtist();
		Artist artist2 = mf2.getTrack().getArtist();
		Artist artist3 = mf3.getTrack().getArtist();
		
		createMusicFiles(asList(mf1, mf2, mf3));
		dao.createTopTags(artist1, asList(new Tag("folk", (short) 38)));
		
		List<Artist> artists = dao.getArtistsWithoutTopTags();

		Assert.assertNotNull(artists);
		Assert.assertFalse(artists.contains(artist1));
		Assert.assertTrue(artists.contains(artist2));
		Assert.assertTrue(artists.contains(artist3));
	}
	
	@Test
	public void artistWithoutMusicFilesAreNotReturned() {
		long time = System.currentTimeMillis();
		MusicFile mf = new MusicFile("Jay Munly", "My Darling Sambo", 
				"/jay munly/my darling sambo", time, time);

		createMusicFiles(Arrays.asList(mf));
		
		musicDao.getArtistId("Jay Farrar");
		
		List<Artist> artists = dao.getArtistsWithoutTopTags();
		Assert.assertNotNull(artists);
		Assert.assertEquals(1, artists.size());
		Assert.assertTrue(mf.getTrack().getArtist().getName()
				.equals(artists.get(0).getName()));
	}

	private void createMusicFiles(List<MusicFile> musicFiles) {
		musicFileDao.clearImport();
		musicFileDao.addMusicFiles(musicFiles);
		musicFileDao.createMusicFiles();
	}
	
	private void deleteArtists() {
		dao.getJdbcTemplate().execute("truncate music.artist cascade");
	}
	
	private void deleteArtistTopTags() {
		dao.getJdbcTemplate().execute("truncate music.artisttoptag cascade");
	}
	
}