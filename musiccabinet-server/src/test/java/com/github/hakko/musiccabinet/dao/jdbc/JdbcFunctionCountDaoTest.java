package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.COUNT_ALL_FUNCTIONS;
import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.COUNT_NAMED_FUNCTION;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;

/**
 * This test verifies the automated process of creating a database function.
 * If a previous function version exists, it is dropped.
 * 
 * Procedure:
 * - load a function to database, call it, and check expected outcome.
 * - load a different, identically named, function to database, call it,
 *   and check expected outcome.
 *   
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcFunctionCountDaoTest {

	@Autowired
	private JdbcFunctionCountDao dao;
	
	@Test
	public void loadAndCallTwoFunctionVersions() throws Exception {
		// load and call a function that counts the total number of functions in the database.
		PostgreSQLUtil.loadFunction(dao, COUNT_ALL_FUNCTIONS);
		int nrOfFunctionsInDatabase = dao.countFunctions();
		// this should give us quite a big number.
		assertTrue(nrOfFunctionsInDatabase > 1);
		
		// load and call a function that counts the occurrences of a function with a given name.
		// the function name is the same as for the one above, but the argument list has changed.
		PostgreSQLUtil.loadFunction(dao, COUNT_NAMED_FUNCTION);
		int nrOfFunctionsWithGivenName = 
			dao.countFunctionsByName(COUNT_NAMED_FUNCTION.getFunctionName());
		// this should give us just one function (namely, itself).
		assertEquals(nrOfFunctionsWithGivenName, 1);
	}
	
}