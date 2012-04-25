package com.github.hakko.musiccabinet.dao.jdbc;

import java.io.IOException;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.DatabaseAdministrationService;
import com.github.hakko.musiccabinet.util.ResourceUtil;

/*
 * This test class verifies the detection of a running postgresql service,
 * and detection/creating of a missing database.
 * 
 * Therefore, it is not meant to be run as part of the build process, and
 * the @Test annotations are commented.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcDatabaseAdministrationDaoEnvironmentDependentTest {

	@Autowired
	private JdbcDatabaseAdministrationDao dbAdmDao;
	
//	stop postgresql service before running this test, or it will fail.
//	@Test
	public void postgresqlServiceDownIsDetected() {
		Assert.assertFalse(dbAdmDao.isRDBMSRunning());
	}

	@Test
	public void postgresqlServiceUpIsDetected() {
		Assert.assertTrue(dbAdmDao.isRDBMSRunning());
	}

	
//	delete database "musiccabinet-test" before running this test, or it will fail.
//	@Test
	public void missingDatabaseIsDetectedAndCreated() throws ApplicationException {
		Assert.assertFalse(dbAdmDao.isDatabaseCreated());

		dbAdmDao.createEmptyDatabase();
		
		Assert.assertTrue(dbAdmDao.isDatabaseCreated());
		Assert.assertEquals(0, dbAdmDao.getDatabaseVersion());

		DatabaseAdministrationService service = new DatabaseAdministrationService();
		service.setDatabaseAdministrationDao(dbAdmDao);
		service.loadNewDatabasUpdates();
	}

	@Test
	public void userWithCredentialsCanLogIn() {
		Assert.assertTrue(dbAdmDao.isPasswordCorrect(getPostgresPassword()));
	}

	@Test
	public void userWithoutCredentialsCannotLogIn() {
		Assert.assertFalse(dbAdmDao.isPasswordCorrect("wrong password"));
	}

	@Test (expected = ApplicationException.class)
	public void forcePasswordChangeDoesntWorkForWrongPassword() throws ApplicationException {
		dbAdmDao.forcePasswordUpdate("wrong password");
		Assert.fail("Wrong password is supposed to throw an exception.");
	}

	@Test
	public void forcePasswordChangeWorksForCorrectPassword() {
		try {
			dbAdmDao.forcePasswordUpdate(getPostgresPassword());
		} catch (ApplicationException e) {
			Assert.fail("Correct password shouldn't throw an exception!");
		}
	}

	private String getPostgresPassword() {
		Properties props = new Properties();
		try {
			props.load(new ResourceUtil("local.jdbc.properties").getInputStream());
		} catch (IOException e) {
			Assert.fail("IOException encountered while reading password!");
		}
		String password = props.getProperty("musiccabinet.jdbc.password");
		Assert.assertNotNull(password);
		return password;
	}
}