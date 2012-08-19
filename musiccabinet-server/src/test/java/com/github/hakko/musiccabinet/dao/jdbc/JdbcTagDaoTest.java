package com.github.hakko.musiccabinet.dao.jdbc;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.domain.model.aggr.TagTopArtists;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcTagDaoTest {

	@Autowired
	private JdbcTagDao dao;
	
	@Test
	public void topTagsReturnsAnUnspecifiedList() {
		dao.getTopTags();
	}

	@Test
	public void canStoreAndRetrieveSingleTopTag() {
		deleteTags();
		
		List<String> singleTag = Arrays.asList("disco");
		
		dao.createTags(singleTag);
		dao.setTopTags(singleTag);
		List<String> topTags = dao.getTopTags();
		
		Assert.assertNotNull(topTags);
		Assert.assertEquals(1, topTags.size());
		Assert.assertEquals(singleTag.get(0), topTags.get(0));
	}

	@Test
	public void canStoreAndRetrieveMultipleTopTag() {
		deleteTags();
		
		String tag1 = "disco", tag2 = "pop", tag3 = "americana";
		
		List<String> multipleTags = Arrays.asList(tag1, tag2, tag3);
		
		dao.createTags(multipleTags);
		dao.setTopTags(multipleTags);
		List<String> topTags = dao.getTopTags();
		
		Assert.assertNotNull(topTags);
		Assert.assertEquals(3, topTags.size());
		Assert.assertTrue(topTags.contains(tag1));
		Assert.assertTrue(topTags.contains(tag2));
		Assert.assertTrue(topTags.contains(tag3));
	}
	
	@Test
	public void canStoreAndRetrieveTagIds() {
		deleteTags();
		
		List<String> tagNames = Arrays.asList("disco", "sludge");
		dao.createTags(tagNames);
		
		List<Tag> tags = dao.getTags();
		
		Assert.assertEquals(2, tags.size());
		Assert.assertFalse(tags.get(0).getId() == tags.get(1).getId());
		Assert.assertFalse(tags.get(0).getName().equals(tags.get(1).getName()));
		
		for (String tagName : tagNames) {
			Assert.assertTrue(
					tagName.equals(tags.get(0).getName()) ||
					tagName.equals(tags.get(1).getName()));
		}
	}

	@Test
	public void tagsInitiallyDontHaveTopArtists() {
		deleteTags();
		
		List<String> tagNames = Arrays.asList("disco", "sludge");
		dao.createTags(tagNames);
		dao.setTopTags(tagNames);
		
		List<Tag> tags = dao.getTagsWithoutTopArtists();

		Assert.assertEquals(2, tags.size());
	}

	@Test
	public void tagsWithTopArtistsAreNotPickedForUpdate() {
		deleteTags();
		
		List<String> tagNames = Arrays.asList("disco", "sludge");
		dao.createTags(tagNames);
		dao.setTopTags(tagNames);

		List<TagTopArtists> topArtists = Arrays.asList(
				new TagTopArtists("disco", asList(new Artist("Madonna"))));
		dao.createTopArtists(topArtists);
		
		List<Tag> tags = dao.getTagsWithoutTopArtists();

		Assert.assertEquals(1, tags.size());
		Assert.assertEquals("sludge", tags.get(0).getName());
	}

	private void deleteTags() {
		dao.getJdbcTemplate().execute("truncate music.tag cascade");
	}

}