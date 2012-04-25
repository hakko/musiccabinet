package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_ARTISTTOPTRACK_FROM_IMPORT;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

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
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTracksParser;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTracksParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcArtistTopTracksDaoTest {

	@Autowired
	private MusicDao musicDao;
	
	@Autowired
	private MusicFileDao musicFileDao;

	@Autowired
	private JdbcArtistTopTracksDao dao;

	// testdata
	private Artist cherArtist;
	private Artist rihannaArtist;
	private List<Track> cherTopTracks;
	private List<Track> rihannaTopTracks;
	
	private static final String CHER_TOP_TRACKS = "last.fm/xml/toptracks.cher.xml";
	private static final String RIHANNA_TOP_TRACKS = "last.fm/xml/toptracks.rihanna.xml";

	@Before
	public void loadFunctionDependencyAndTestdata() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_ARTISTTOPTRACK_FROM_IMPORT);

		ArtistTopTracksParser cherParser = new ArtistTopTracksParserImpl(
				new ResourceUtil(CHER_TOP_TRACKS).getInputStream());
		ArtistTopTracksParser rihannaParser = new ArtistTopTracksParserImpl(
				new ResourceUtil(RIHANNA_TOP_TRACKS).getInputStream());
		cherArtist = cherParser.getArtist();
		rihannaArtist = rihannaParser.getArtist();
		cherTopTracks = cherParser.getTopTracks();
		rihannaTopTracks = rihannaParser.getTopTracks();

		PostgreSQLUtil.truncateTables(dao);
	}

	@Test
	public void topTracksAreEmptyAfterClearing() throws ApplicationException {
		deleteArtistTopTracks();
	
		List<Track> cherStoredTracks = dao.getTopTracks(cherArtist);
		List<Track> rihannaStoredTracks = dao.getTopTracks(rihannaArtist);
		assertNotNull(cherStoredTracks);
		assertNotNull(rihannaStoredTracks);
		assertEquals(0, cherStoredTracks.size());
		assertEquals(0, rihannaStoredTracks.size());
	}

	@Test
	public void storeAndValidateTopTracks() throws ApplicationException {
		deleteArtistTopTracks();
		
		dao.createTopTracks(cherArtist, cherTopTracks);
		List<Track> cherStoredTracks = dao.getTopTracks(cherArtist);
		assertEquals(cherTopTracks.size(), cherStoredTracks.size());
		for (int i = 0; i < cherTopTracks.size(); i++) {
			assertEquals(cherTopTracks.get(i), cherStoredTracks.get(i));
		}
		
		List<Track> rihannaStoredTracks = dao.getTopTracks(rihannaArtist);
		assertEquals(0, rihannaStoredTracks.size());
	}

	@Test
	public void storeUpdateAndValidateTopTracks() throws ApplicationException {
		deleteArtistTopTracks();
		
		dao.createTopTracks(cherArtist, cherTopTracks);
		dao.createTopTracks(rihannaArtist, rihannaTopTracks);
		
		rihannaTopTracks = new ArrayList<Track>();
		rihannaTopTracks.add(new Track("Rihanna", "Umbrella"));
		rihannaTopTracks.add(new Track("Rihanna", "Don't Stop The Music"));
		dao.createTopTracks(rihannaArtist, rihannaTopTracks);
		
		List<Track> cherStoredTopTracks = dao.getTopTracks(cherArtist);
		List<Track> rihannaStoredTopTracks = dao.getTopTracks(rihannaArtist);

		assertEquals(50, cherStoredTopTracks.size());
		assertEquals(2, rihannaStoredTopTracks.size());
		
		for (int i = 0; i < cherTopTracks.size(); i++) {
			assertEquals(cherTopTracks.get(i), cherStoredTopTracks.get(i));
		}
		for (int i = 0; i < rihannaTopTracks.size(); i++) {
			assertEquals(rihannaTopTracks.get(i), rihannaStoredTopTracks.get(i));
		}
	}
	
	@Test
	public void noArtistsMeanNoMissingTopTracks() {
		deleteArtists();
		
		List<Artist> artists = dao.getArtistsWithoutTopTracks();
		Assert.assertNotNull(artists);
		Assert.assertEquals(0, artists.size());
	}
	
	@Test
	public void oneArtistMeansOneMissingTopTracks() {
		final String artistName = "Piano Magic";
		final String trackName = "Kingfisher / Grass";
		final String path = "/";
		final long lastModified = System.currentTimeMillis(), created = lastModified;
		
		deleteArtists();
		
		createMusicFiles(Arrays.asList(
				new MusicFile(artistName, trackName, path, created, lastModified)));
		
		List<Artist> artists = dao.getArtistsWithoutTopTracks();
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
		dao.createTopTracks(artist1, asList(mf1.getTrack()));
		
		List<Artist> artists = dao.getArtistsWithoutTopTracks();

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
		
		List<Artist> artists = dao.getArtistsWithoutTopTracks();
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
	
	private void deleteArtistTopTracks() {
		dao.getJdbcTemplate().execute("truncate music.artisttoptrack cascade");
	}
	
}