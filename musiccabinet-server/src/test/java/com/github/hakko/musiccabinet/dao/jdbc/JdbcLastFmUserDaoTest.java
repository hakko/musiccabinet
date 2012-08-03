package com.github.hakko.musiccabinet.dao.jdbc;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcLastFmUserDaoTest {

	@Autowired
	private JdbcLastFmUserDao dao;

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
	
}