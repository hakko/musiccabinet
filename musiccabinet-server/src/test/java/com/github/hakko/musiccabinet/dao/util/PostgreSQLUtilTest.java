package com.github.hakko.musiccabinet.dao.util;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class PostgreSQLUtilTest {

	@Test
	public void getParametersScalesWell() {

		String oneParameter = PostgreSQLUtil.getParameters(1);
		String twoParameters = PostgreSQLUtil.getParameters(2);
		String sixParameters = PostgreSQLUtil.getParameters(6);
		String tenParameters = PostgreSQLUtil.getParameters(10);
		
		Assert.assertEquals(oneParameter, "?");
		Assert.assertEquals(twoParameters, "?,?");
		Assert.assertEquals(sixParameters, "?,?,?,?,?,?");
		Assert.assertEquals(tenParameters, "?,?,?,?,?,?,?,?,?,?");
	}

}