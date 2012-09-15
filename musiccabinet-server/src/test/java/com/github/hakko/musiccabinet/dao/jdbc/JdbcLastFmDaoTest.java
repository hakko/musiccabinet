package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_LASTFMGROUP;
import static java.util.Arrays.asList;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcLastFmDaoTest {

	@Autowired
	private JdbcLastFmDao dao;
	
	@Test
	public void storesAndRetrievesUser() {
		LastFmUser user = new LastFmUser("username", "sessionkey");
		
		dao.createOrUpdateLastFmUser(user);
		LastFmUser dbUser = dao.getLastFmUser(user.getLastFmUsername());
		
		Assert.assertNotNull(dbUser);
		Assert.assertEquals(user.getLastFmUsername(), dbUser.getLastFmUsername());
		Assert.assertEquals(user.getSessionKey(), dbUser.getSessionKey());
		
		user.setSessionKey("another session key");
		dao.createOrUpdateLastFmUser(user);
		dbUser = dao.getLastFmUser(user.getLastFmUsername());

		Assert.assertNotNull(dbUser);
		Assert.assertEquals(user.getLastFmUsername(), dbUser.getLastFmUsername());
		Assert.assertEquals(user.getSessionKey(), dbUser.getSessionKey());
	}
	
	@Test
	public void storesAndRetrievesGroups() throws ApplicationException {
		dao.getJdbcTemplate().execute("truncate music.lastfmgroup cascade");
		PostgreSQLUtil.loadFunction(dao, UPDATE_LASTFMGROUP);
		
		List<LastFmGroup> groups;
		
		groups = dao.getLastFmGroups();
		Assert.assertNotNull(groups);
		Assert.assertEquals(0, groups.size());
		
		dao.setLastFmGroups(asList(new LastFmGroup("G1"), new LastFmGroup("G2")));
		groups = dao.getLastFmGroups();
		Assert.assertEquals(2, groups.size());
		Assert.assertEquals("G1", groups.get(0).getName());
		Assert.assertEquals("G2", groups.get(1).getName());
		
		dao.setLastFmGroups(asList(new LastFmGroup("G2"), new LastFmGroup("G3")));
		groups = dao.getLastFmGroups();
		Assert.assertEquals(2, groups.size());
		Assert.assertEquals("G2", groups.get(0).getName());
		Assert.assertEquals("G3", groups.get(1).getName());
	}

	@Test
	public void voidReEnablesGroups() throws ApplicationException {
		dao.getJdbcTemplate().execute("truncate music.lastfmgroup cascade");
		PostgreSQLUtil.loadFunction(dao, UPDATE_LASTFMGROUP);
		
		List<LastFmGroup> groups;
		
		dao.setLastFmGroups(asList(new LastFmGroup("G1"), new LastFmGroup("G2")));
		groups = dao.getLastFmGroups();
		Assert.assertEquals(2, groups.size());
		Assert.assertEquals("G1", groups.get(0).getName());
		Assert.assertEquals("G2", groups.get(1).getName());

		dao.setLastFmGroups(asList(new LastFmGroup("G1")));
		groups = dao.getLastFmGroups();
		Assert.assertEquals(1, groups.size());
		Assert.assertEquals("G1", groups.get(0).getName());

		dao.setLastFmGroups(asList(new LastFmGroup("G1"), new LastFmGroup("G2")));
		groups = dao.getLastFmGroups();
		Assert.assertEquals(2, groups.size());
		Assert.assertEquals("G1", groups.get(0).getName());
		Assert.assertEquals("G2", groups.get(1).getName());
	}
}