package com.github.hakko.musiccabinet.dao.jdbc;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

	private void deleteTags() {
		dao.getJdbcTemplate().execute("truncate music.tag cascade");
	}

}