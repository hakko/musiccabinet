package com.github.hakko.musiccabinet.service;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class SearchIndexUpdateExecutorServiceTest {

	@Autowired
	private SearchIndexUpdateExecutorService executorService;

	private static int ids = 0;

	@Test
	public void parallellUpdatesGetThrottled() {
		List<SearchIndexUpdateService> updateServices = new ArrayList<SearchIndexUpdateService>();
		for (int i = 0; i < 5; i++) {
			updateServices.add(new TestUpdateService());
		}
		executorService.updateSearchIndex(updateServices);
		
		int totalOperations = 0, finishedOperations = 0;
		
		for (SearchIndexUpdateService updateService : updateServices) {
			totalOperations += updateService.getProgress().getTotalOperations();
			finishedOperations += updateService.getProgress().getFinishedOperations();
		}
		
		Assert.assertEquals(5+4+3+2+1, totalOperations);
		Assert.assertEquals(totalOperations, finishedOperations);
	
//		TODO : validate actual blocking. testing was made harder when permits are handed out per minute		
//		Assert.assertTrue(ms / 1000 >= 3); // 5+4+3+2+1 operations = 15, 5/sec -> 3 sec
	}
	
	private class TestUpdateService extends SearchIndexUpdateService {

		private int id = ++ids;
		
		@Override
		public String getUpdateDescription() {
			return "test updates";
		}

		@Override
		public void updateSearchIndex() throws ApplicationException {
			setTotalOperations(id);
			for (int i = 0; i < id; i++) {
				executorService.getThrottleService().awaitAllowance();
				addFinishedOperation();
			}
		}
		
	}
	
}