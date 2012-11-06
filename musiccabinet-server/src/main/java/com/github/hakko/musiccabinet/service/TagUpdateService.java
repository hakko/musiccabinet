package com.github.hakko.musiccabinet.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.hakko.musiccabinet.dao.ArtistTopTagsDao;
import com.github.hakko.musiccabinet.dao.LastFmDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistUserTag;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.ws.lastfm.TagUpdateClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Provides services for updating artist tags.
 */
public class TagUpdateService {

	protected LastFmDao lastFmDao;
	protected ArtistTopTagsDao artistTopTagsDao;

	protected TagUpdateClient tagUpdateClient;
	
	private AtomicBoolean started = new AtomicBoolean(false);

	private ConcurrentLinkedQueue<ArtistUserTag> artistUserTags = new ConcurrentLinkedQueue<>();
	protected List<ArtistUserTag> failedUpdates = new ArrayList<>();

	public static final int MIN_THRESHOLD = 10;
	public static final int MAX_THRESHOLD = 90;
	
	private static final Logger LOG = Logger.getLogger(TagUpdateService.class);

	/*
	 * Async method that registers tag updates for later submission, in case
	 * last.fm is down.
	 */
	public void updateTag(Artist artist, String lastFmUsername,
			String tagName, int tagCount, boolean submit) {
		LastFmUser lastFmUser = lastFmDao.getLastFmUser(lastFmUsername);
		register(new ArtistUserTag(artist, lastFmUser, tagName, tagCount, submit));

		if (!started.getAndSet(true)) {
			startTagUpdateService();
		}
	}

	private void startTagUpdateService() {

		ScheduledExecutorService scheduler = Executors
				.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					updateTags();
				} catch (Throwable t) {
					LOG.error("Unexpected error caught while sending tag updates!", t);
				}
			}
		}, 1, 1, TimeUnit.MINUTES);

	}

	protected void register(ArtistUserTag submission) {
		artistTopTagsDao.updateTopTag(submission.getArtist().getId(), 
				submission.getTagName(), submission.getTagCount());
		for (Iterator<ArtistUserTag> it = artistUserTags.iterator(); it.hasNext();) {
			ArtistUserTag aut = it.next();
			if (aut.getArtist().getId() == submission.getArtist().getId()
					&& aut.getLastFmUser().getId() == submission.getLastFmUser().getId()) {
				it.remove();
				LOG.debug("remove " + aut + ", in favor of " + submission);
			}
		}
		artistUserTags.add(submission);
	}

	protected void updateTags() throws ApplicationException {
		resendFailedUpdates();
		ArtistUserTag aut;
		while ((aut = artistUserTags.poll()) != null) {
			if ((aut.isIncrease() && aut.getTagCount() >= MAX_THRESHOLD) ||
				(!aut.isIncrease() && aut.getTagCount() <= MIN_THRESHOLD)) {
				WSResponse wsResponse = tagUpdateClient.updateTag(aut);
				if (!wsResponse.wasCallSuccessful()) {
					LOG.warn("updating " + aut + " failed! Add for re-sending.");
					LOG.debug("Response: " + wsResponse);
					failedUpdates.add(aut);
				}
			}
		}
	}

	protected void resendFailedUpdates() throws ApplicationException {
		while (failedUpdates.size() > 0) {
			LOG.debug("Queue of failed updates consists of "
					+ failedUpdates.size() + " elements.");
			ArtistUserTag firstFailed = failedUpdates.get(0);
			WSResponse wsResponse = tagUpdateClient.updateTag(firstFailed);
			if (wsResponse.wasCallSuccessful()) {
				LOG.debug("Failed tag update was re-sent.");
				failedUpdates.remove(0);
			} else {
				LOG.debug("Failed tag update could not be re-sent. Wait a minute before trying again.");
				LOG.debug("Response: " + wsResponse);
				return;
			}
		}
	}

	// Spring setters

	public void setLastFmDao(LastFmDao lastFmDao) {
		this.lastFmDao = lastFmDao;
	}

	public void setArtistTopTagsDao(ArtistTopTagsDao artistTopTagsDao) {
		this.artistTopTagsDao = artistTopTagsDao;
	}

	public void setTagUpdateClient(TagUpdateClient tagUpdateClient) {
		this.tagUpdateClient = tagUpdateClient;
	}

}