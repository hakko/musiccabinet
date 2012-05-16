package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_TAGINFO;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.music.TagInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.TagInfoParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcTagInfoDaoTest {

	/* Test data */
	private TagInfo tiDisco, tiPop, tiSludge;

	private static final String TI_DISCO_FILE = 
			"last.fm/xml/taginfo.disco.xml";
	private static final String TI_POP_FILE = 
			"last.fm/xml/taginfo.pop.xml";
	private static final String TI_SLUDGE_FILE = 
			"last.fm/xml/taginfo.sludge.xml";

	// expected outcome from disco file
	private static final String DISCO_SUMMARY = "Disco is a genre of dance-oriented music that originated in African American, gay and Hispanic American communities in 1970s. In what is considered a forerunner to disco style clubs in February 1970 New York City DJ David Mancuso opened The Loft, a members-only private dance club set in his own home. Most agree that the first disco songs were released in 1973, though some claim that Soul Makossa by <span title=\"Unknown artist\" class=\"bbcode_unknown\">Manu Dibango's</span> from 1972 to be the first disco record. The first article about disco was written in September 1973 by Vince Aletti for Rolling Stone Magazine.";
	
	@Autowired
	private JdbcTagInfoDao dao;
	
	@Autowired
	private JdbcTagDao tagDao;
	
	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_TAGINFO);
		
		tiDisco = new TagInfoParserImpl(new ResourceUtil(
				TI_DISCO_FILE).getInputStream()).getTagInfo();
		tiPop = new TagInfoParserImpl(new ResourceUtil(
				TI_POP_FILE).getInputStream()).getTagInfo();
		tiSludge = new TagInfoParserImpl(new ResourceUtil(
				TI_SLUDGE_FILE).getInputStream()).getTagInfo();

		deleteTags();
		
		tagDao.createTags(Arrays.asList(tiDisco.getTagName(), 
				tiPop.getTagName(), tiSludge.getTagName()));
	}
	
	@Test
	public void storedTagInfosAreReturnedWithSummaryDescription() {
		deleteTagInfos();

		String discoInfo = dao.getTagInfo(tiDisco.getTagName());
		Assert.assertNull(discoInfo);
		
		dao.createTagInfo(Arrays.asList(tiDisco));
		discoInfo = dao.getTagInfo(tiDisco.getTagName());
		Assert.assertNotNull(discoInfo);
		Assert.assertEquals(DISCO_SUMMARY, discoInfo);
	}
	
	@Test
	public void multipleTagInfosCanBeCreatedAndRetrieved() {
		deleteTagInfos();

		String discoInfo = dao.getTagInfo(tiDisco.getTagName());
		String popInfo = dao.getTagInfo(tiPop.getTagName());

		Assert.assertNull(discoInfo);
		Assert.assertNull(popInfo);
		
		dao.createTagInfo(Arrays.asList(tiDisco, tiPop));

		discoInfo = dao.getTagInfo(tiDisco.getTagName());
		popInfo = dao.getTagInfo(tiPop.getTagName());

		Assert.assertNotNull(discoInfo);
		Assert.assertNotNull(popInfo);
	}
	
	@Test
	public void canFindTagsWithInfo() {
		deleteTagInfos();

		List<String> tagsWithInfo = dao.getTagsWithInfo();
		Assert.assertNotNull(tagsWithInfo);
		Assert.assertEquals(0, tagsWithInfo.size());

		dao.createTagInfo(Arrays.asList(tiDisco));
		tagsWithInfo = dao.getTagsWithInfo();
		Assert.assertEquals(1, tagsWithInfo.size());
		Assert.assertTrue(tagsWithInfo.contains(tiDisco.getTagName()));
		Assert.assertFalse(tagsWithInfo.contains(tiPop.getTagName()));
		Assert.assertFalse(tagsWithInfo.contains(tiSludge.getTagName()));
		
		dao.createTagInfo(Arrays.asList(tiPop, tiSludge));
		Assert.assertEquals(3, dao.getTagsWithInfo().size());
	}

	private void deleteTags() {
		dao.getJdbcTemplate().execute("truncate music.tag cascade");
	}

	private void deleteTagInfos() {
		dao.getJdbcTemplate().execute("truncate music.taginfo cascade");
	}
	
}