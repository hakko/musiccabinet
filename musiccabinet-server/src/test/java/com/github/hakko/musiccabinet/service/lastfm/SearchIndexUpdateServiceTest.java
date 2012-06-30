package com.github.hakko.musiccabinet.service.lastfm;

import junit.framework.Assert;

import org.junit.Test;

import com.github.hakko.musiccabinet.domain.model.aggr.SearchIndexUpdateProgress;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.lastfm.SearchIndexUpdateService;

public class SearchIndexUpdateServiceTest {

	@Test
	public void successfulServiceInvocation() {
		SuccessfulUpdateService successfulService = new SuccessfulUpdateService();
		
		SearchIndexUpdateProgress progress;
		Assert.assertNotNull(progress = successfulService.getProgress());
		Assert.assertEquals(SearchIndexUpdateProgress.NOT_INITIALIZED, progress.getTotalOperations());
		Assert.assertEquals(SearchIndexUpdateProgress.NOT_INITIALIZED, progress.getFinishedOperations());

		successfulService.updateSearchIndex();
		
		Assert.assertNotNull(progress = successfulService.getProgress());
		Assert.assertEquals(SuccessfulUpdateService.TOTAL_CALCULATIONS, progress.getTotalOperations());
		Assert.assertEquals(SuccessfulUpdateService.TOTAL_CALCULATIONS, progress.getFinishedOperations());
		
		successfulService.reset();
		Assert.assertNotNull(progress = successfulService.getProgress());
		Assert.assertEquals(SearchIndexUpdateProgress.NOT_INITIALIZED, progress.getTotalOperations());
		Assert.assertEquals(SearchIndexUpdateProgress.NOT_INITIALIZED, progress.getFinishedOperations());
	}

	@Test
	public void failingServiceInvocation() {
		FailingUpdateService failingService = new FailingUpdateService();
		
		SearchIndexUpdateProgress progress;
		Assert.assertNotNull(progress = failingService.getProgress());
		Assert.assertEquals(SearchIndexUpdateProgress.NOT_INITIALIZED, progress.getTotalOperations());
		Assert.assertEquals(SearchIndexUpdateProgress.NOT_INITIALIZED, progress.getFinishedOperations());

		try {
			failingService.updateSearchIndex();
			Assert.fail();
		} catch (ApplicationException e) {
		}
			
		Assert.assertNotNull(progress = failingService.getProgress());
		Assert.assertEquals(FailingUpdateService.TOTAL_CALCULATIONS, progress.getTotalOperations());
		Assert.assertEquals(FailingUpdateService.FAIL_INDEX, progress.getFinishedOperations());
		
		failingService.reset();
		Assert.assertNotNull(progress = failingService.getProgress());
		Assert.assertEquals(SearchIndexUpdateProgress.NOT_INITIALIZED, progress.getTotalOperations());
		Assert.assertEquals(SearchIndexUpdateProgress.NOT_INITIALIZED, progress.getFinishedOperations());
	}

	/*
	 * Simple test service, that does 10 rounds of calculations and finishes normally.
	 */
	protected class SuccessfulUpdateService extends SearchIndexUpdateService {

		public static final int TOTAL_CALCULATIONS = 10;
		
		@Override
		public String getUpdateDescription() {
			return "test runs";
		}

		@Override
		public void updateSearchIndex() {
			setTotalOperations(TOTAL_CALCULATIONS);
			for (int i = 0; i < TOTAL_CALCULATIONS; i++) {
				// real work would go here
				addFinishedOperation();
			}
		}
		
	}

	/*
	 * Simple test service, that intends to do 1000 rounds of calculations
	 * but fails after 25 and throws an exception.
	 */
	protected class FailingUpdateService extends SearchIndexUpdateService {

		public static final int TOTAL_CALCULATIONS = 1000;
		public static final int FAIL_INDEX = 25;
		
		@Override
		public String getUpdateDescription() {
			return "test runs";
		}

		@Override
		public void updateSearchIndex() throws ApplicationException {
			setTotalOperations(TOTAL_CALCULATIONS);
			for (int i = 0; i < TOTAL_CALCULATIONS; i++) {
				// real work would go here
				if (i == FAIL_INDEX) {
					throw new ApplicationException("Failing at index " + FAIL_INDEX);
				}
				addFinishedOperation();
			}
		}
		
	}

}