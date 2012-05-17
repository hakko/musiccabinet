package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_ARTISTTOPTRACK;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
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
		PostgreSQLUtil.loadFunction(dao, UPDATE_ARTISTTOPTRACK);

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
	
	private void deleteArtistTopTracks() {
		dao.getJdbcTemplate().execute("truncate music.artisttoptrack cascade");
	}
	
}