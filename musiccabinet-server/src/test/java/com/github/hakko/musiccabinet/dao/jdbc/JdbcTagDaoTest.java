package com.github.hakko.musiccabinet.dao.jdbc;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	@Test
	public void createsTagCorrections() {
		deleteTags();
		
		dao.createTags(Arrays.asList("disco", "80s", "pop"));
		
		Map<String, String> corrections = new HashMap<>();
		corrections.put("80s", "disco");
		dao.createTagCorrections(corrections);
		
		Map<String, String> correctedTags = dao.getCorrectedTags();
		
		Assert.assertNotNull(correctedTags);
		Assert.assertEquals(1, correctedTags.size());
		Assert.assertTrue(correctedTags.containsKey("80s"));
		Assert.assertEquals("disco", correctedTags.get("80s"));
	}
	
	@Test
	public void createsNewTagsWhenCreatingCorrections() {
		deleteTags();
		
		Map<String, String> corrections = new HashMap<>();
		corrections.put("sludge", "drone");
		
		dao.createTags(Arrays.asList("disco", "sludge"));
		dao.createTagCorrections(corrections);
		
		List<Tag> tags = dao.getTags();
		Assert.assertNotNull(tags);
		Assert.assertEquals(3, tags.size());
		for (String tagName : Arrays.asList("disco", "sludge", "drone")) {
			Assert.assertTrue(tags.contains(new Tag(tagName, (short) 0)));
		}
	}
	
	private void deleteTags() {
		dao.getJdbcTemplate().execute("truncate music.tag cascade");
	}

}