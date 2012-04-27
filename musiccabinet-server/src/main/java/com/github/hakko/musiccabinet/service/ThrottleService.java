package com.github.hakko.musiccabinet.service;

import java.util.concurrent.Semaphore;

import com.github.hakko.musiccabinet.log.Logger;

/*
 * ThrottleService is the single point of decision whether last.fm calls
 * are currently allowed.
 * 
 * The terms of service states that a maximum of five calls per second,
 * averaging over a five minute period is allowed.
 * 
 * To play it safe, we stick to allowing max five calls per second (not
 * adding up for the fluctuation).
 * 
 * TODO : maybe hand out 60 * 5 permits once per minute?
 */
public class ThrottleService {

	private Semaphore semaphore = new Semaphore(0, true);

	private static final int THRESHOLD = 5;

	private Logger LOG = Logger.getLogger(ThrottleService.class);
	
	public void awaitAllowance() {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			LOG.warn("Throttle semaphore acquire interrupted!", e);
		}
	}
	
	public void allowCalls() {
		semaphore.release(60 * THRESHOLD - semaphore.availablePermits());
	}

}