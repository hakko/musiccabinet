package com.github.hakko.musiccabinet.service;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class DatabaseAdministrationServiceTest {

	@Autowired
	private DatabaseAdministrationService dbAdmService;

	@Test
	public void serviceConfigured() {
		Assert.assertNotNull(dbAdmService);
	}

	@Test
	public void serviceFindsAllDatabaseUpdates() {
		List<Integer> updates = dbAdmService.getDatabaseUpdates();

		final int NR_OF_UPDATES = 39;

		Assert.assertNotNull(updates);
		Assert.assertEquals(NR_OF_UPDATES, updates.size());
		for (int i = 0; i < NR_OF_UPDATES; i++) {
			Assert.assertEquals(1000 + i, updates.get(i).intValue());
		}
	}

}