package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_ARTISTTOPTAG;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

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
	
	private void deleteArtistTopTags() {
		dao.getJdbcTemplate().execute("truncate music.artisttoptag cascade");
	}
	
}