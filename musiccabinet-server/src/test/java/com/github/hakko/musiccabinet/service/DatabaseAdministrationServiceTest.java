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
		
		Assert.assertNotNull(updates);
		Assert.assertEquals(8, updates.size());
		Assert.assertEquals(1000, updates.get(0).intValue());
		Assert.assertEquals(1001, updates.get(1).intValue());
		Assert.assertEquals(1002, updates.get(2).intValue());
		Assert.assertEquals(1003, updates.get(3).intValue());
		Assert.assertEquals(1004, updates.get(4).intValue());
		Assert.assertEquals(1005, updates.get(5).intValue());
		Assert.assertEquals(1006, updates.get(6).intValue());
		Assert.assertEquals(1007, updates.get(7).intValue());
	}
	
}