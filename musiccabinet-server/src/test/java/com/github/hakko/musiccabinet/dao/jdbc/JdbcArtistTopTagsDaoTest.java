package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_ARTISTTOPTAG;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
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
	private JdbcArtistTopTagsDao dao;

	@Autowired
	private JdbcMusicDao musicDao;

	@Autowired
	private JdbcTagDao tagDao;
	
	// testdata
	private Artist cherArtist;
	private Artist rihannaArtist;
	private List<Tag> cherTopTags;
	private List<Tag> rihannaTopTags;
	
	private static final String CHER_TOP_TAGS = "last.fm/xml/toptags.cher.xml";
	private static final String RIHANNA_TOP_TAGS = "last.fm/xml/toptags.rihanna.xml";

	@Before
	public void loadFunctionDependencyAndTestdata() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_ARTISTTOPTAG);

		ArtistTopTagsParser cherParser = new ArtistTopTagsParserImpl(
				new ResourceUtil(CHER_TOP_TAGS).getInputStream());
		ArtistTopTagsParser rihannaParser = new ArtistTopTagsParserImpl(
				new ResourceUtil(RIHANNA_TOP_TAGS).getInputStream());
		cherArtist = cherParser.getArtist();
		cherTopTags = cherParser.getTopTags();
		rihannaArtist = rihannaParser.getArtist();
		rihannaTopTags = rihannaParser.getTopTags();

		PostgreSQLUtil.truncateTables(dao);

		musicDao.setArtistId(cherArtist);
		musicDao.setArtistId(rihannaArtist);
	}

	@Test
	public void topTagsAreEmptyAfterClearing() throws ApplicationException {
		deleteArtistTopTags();
	
		List<Tag> cherStoredTracks = dao.getTopTags(cherArtist.getId());
		assertNotNull(cherStoredTracks);
		assertEquals(0, cherStoredTracks.size());
	}

	@Test
	public void storeAndValidateTopTags() throws ApplicationException {
		deleteArtistTopTags();
		
		dao.createTopTags(cherArtist, cherTopTags);
		List<Tag> cherStoredTags = dao.getTopTags(cherArtist.getId());
		assertEquals(cherTopTags.size(), cherStoredTags.size());
		for (int i = 0; i < cherTopTags.size(); i++) {
			assertTrue(cherStoredTags.contains(cherTopTags.get(i)));
		}
		
		List<Tag> rihannaStoredTags = dao.getTopTags(rihannaArtist.getId());
		assertEquals(0, rihannaStoredTags.size());
	}

	@Test
	public void storeUpdateAndValidateTopTags() throws ApplicationException {
		deleteArtistTopTags();
		
		dao.createTopTags(cherArtist, cherTopTags);
		dao.createTopTags(rihannaArtist, rihannaTopTags);
		
		rihannaTopTags = new ArrayList<>();
		rihannaTopTags.add(new Tag("dance", (short) 22));
		rihannaTopTags.add(new Tag("r&b", (short) 8));
		dao.createTopTags(rihannaArtist, rihannaTopTags);
		
		List<Tag> cherStoredTopTags = dao.getTopTags(cherArtist.getId());
		List<Tag> rihannaStoredTopTags = dao.getTopTags(rihannaArtist.getId());
		
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
	public void returnsMostPopularCloudTagsForArtist() throws ApplicationException {
		deleteArtistTopTags();
		
		dao.createTopTags(cherArtist, cherTopTags);
		int cherId = musicDao.getArtistId(cherArtist);

		Tag tagPop = new Tag("pop", (short) 100), tag80s = new Tag("80s", (short) 52);
		tagDao.setTopTags(Arrays.asList(tagPop.getName(), tag80s.getName()));
		
		assertEquals(asList(tagPop), dao.getTopTags(cherId, 1));
		assertEquals(asList(tagPop, tag80s), dao.getTopTags(cherId, 3));
	}
	
	@Test
	public void addsNewTagOnUpdate() {
		deleteArtistTopTags();
		
		dao.updateTopTag(rihannaArtist.getId(), "disco", 99);
		
		List<Tag> topTags = dao.getTopTags(rihannaArtist.getId());
		
		assertEquals(1, topTags.size());
		assertEquals(99, topTags.get(0).getCount());
		assertEquals("disco", topTags.get(0).getName());
	}
	
	@Test
	public void removesUnusedTagOnUpdate() {
		deleteArtistTopTags();
		
		dao.createTopTags(rihannaArtist, rihannaTopTags);
		assertEquals("pop", dao.getTopTags(rihannaArtist.getId()).get(0).getName());
		
		dao.updateTopTag(rihannaArtist.getId(), "pop", 0);
		assertEquals("rnb", dao.getTopTags(rihannaArtist.getId()).get(0).getName());
	}
	
	private void deleteArtistTopTags() {
		dao.getJdbcTemplate().execute("truncate music.artisttoptag cascade");
	}
	
}