package com.github.hakko.musiccabinet.service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;

/*
 * Whenever the last.fm search index is to be updated, the work flow would be:
 * 
 * SearchIndexUpdateExecutorService executorService;
 * executorService.updateSearchIndex(albumInfoService, artistInfoService, ...);
 * executorService.updateSearchIndex(tagInfoService);
 * 
 * The first call allows for parallel execution of all update services, except
 * for tagInfoService, which has to be executed sequentially as it depends
 * on artistTopTagsService for deciding which tags to fetch info for.
 * 
 * The parallel execution is throttled by handing out permits to do last.fm calls
 * every second, and having the update service threads claim such a permit before
 * making a call.
 * 
 * This class is not thread-safe in itself. It is meant to be called once a day.
 */
public class SearchIndexUpdateExecutorService {

	private ThrottleService throttleService;

	private ScheduledExecutorService scheduler;
	private AtomicInteger activeThreads = new AtomicInteger();
	
	private Logger LOG = Logger.getLogger(SearchIndexUpdateExecutorService.class);
	
	public void updateSearchIndex(List<? extends SearchIndexUpdateService> updateServices) {
		final int threads = updateServices.size();
		activeThreads.set(threads);
		
		scheduler = Executors.newScheduledThreadPool(threads + 1);
		scheduler.scheduleAtFixedRate(new Throttler(), 0, 1, TimeUnit.MINUTES);

		for (SearchIndexUpdateService updateService : updateServices) {
			scheduler.execute(new Worker(updateService));
		}
		
		try {
			while (!scheduler.awaitTermination(1, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
		}
	}

	// used to send allowance to invoke last.fm calls, five per second.
	private class Throttler implements Runnable {

		@Override
		public void run() {
			throttleService.allowCalls();
		}
		
	}

	// wraps actual update jobs. shutdowns throttler when last update job is done.
	private class Worker implements Runnable {

		private SearchIndexUpdateService updateService;
		
		public Worker(SearchIndexUpdateService updateService) {
			this.updateService = updateService;
		}
		
		@Override
		public void run() {
			try {
				updateService.updateSearchIndex();
			} catch (ApplicationException e) {
				LOG.warn(updateService + " failed!", e);
			} catch (Throwable t) {
				LOG.error(updateService + " failed with an unexpected error.", t);
			}
			if (activeThreads.decrementAndGet() == 0) {
				scheduler.shutdown();
			}
		}
		
	}

	protected ThrottleService getThrottleService() {
		return throttleService;
	}
	
	// Spring setters

	public void setThrottleService(ThrottleService throttleService) {
		this.throttleService = throttleService;
	}
	
}