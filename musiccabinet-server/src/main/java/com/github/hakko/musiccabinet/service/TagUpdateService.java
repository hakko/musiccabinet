package com.github.hakko.musiccabinet.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.message.GenericMessage;

import com.github.hakko.musiccabinet.dao.ArtistTopTagsDao;
import com.github.hakko.musiccabinet.dao.LastFmDao;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistUserTag;
import com.github.hakko.musiccabinet.domain.model.aggr.TagOccurrence;
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

	private LastFmDao lastFmDao;
	private ArtistTopTagsDao artistTopTagsDao;

	private TagUpdateClient tagUpdateClient;
	
	protected PollableChannel tagUpdateChannel;
	private AtomicBoolean started = new AtomicBoolean(false);

	private ConcurrentLinkedQueue<ArtistUserTag> artistUserTags = new ConcurrentLinkedQueue<>();
	protected List<ArtistUserTag> failedUpdates = new ArrayList<>();

	private static final Logger LOG = Logger.getLogger(TagUpdateService.class);

	/*
	 * Async method that registers tag updates for later submission, in case
	 * last.fm is down.
	 */
	public void updateTag(Artist artist, String lastFmUsername,
			TagOccurrence tagOccurrence) {
		LastFmUser lastFmUser = lastFmDao.getLastFmUser(lastFmUsername);
		ArtistUserTag aut = new ArtistUserTag(artist, lastFmUser, tagOccurrence);

		tagUpdateChannel.send(new GenericMessage<ArtistUserTag>(aut));

		if (!started.getAndSet(true)) {
			startTagUpdateService();
		}
	}

	private void startTagUpdateService() {

		ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
		threadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					receive();
				} catch (Throwable t) {
					LOG.error("Unexpected error caught while receiving tag updates!", t);
				}
			}
		});

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

	@SuppressWarnings("unchecked")
	protected void receive() {
		Message<ArtistUserTag> message;
		while ((message = (Message<ArtistUserTag>) tagUpdateChannel.receive()) != null) {
			ArtistUserTag latest = message.getPayload();
			for (Iterator<ArtistUserTag> it = artistUserTags.iterator(); it.hasNext();) {
				ArtistUserTag aut = it.next();
				if (aut.getArtist().getId() == latest.getArtist().getId()
						&& aut.getLastFmUser().getId() == latest.getLastFmUser().getId()) {
					it.remove();
					LOG.debug("remove " + aut + ", in favor of " + latest);
				}
			}
			artistUserTags.add(latest);
		}
	}

	protected void updateTags() throws ApplicationException {
		resendFailedUpdates();
		ArtistUserTag aut;
		while ((aut = artistUserTags.poll()) != null) {
			artistTopTagsDao.updateTopTag(aut.getArtist(), aut.getTagOccurrence());
			WSResponse wsResponse = tagUpdateClient.updateTag(aut);
			if (!wsResponse.wasCallSuccessful()) {
				LOG.warn("updating " + aut + " failed! Add for re-sending.");
				LOG.debug("Response: " + wsResponse);
				failedUpdates.add(aut);
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

	public void setTagUpdateChannel(PollableChannel tagUpdateChannel) {
		this.tagUpdateChannel = tagUpdateChannel;
	}

	public void setTagUpdateClient(TagUpdateClient tagUpdateClient) {
		this.tagUpdateClient = tagUpdateClient;
	}

}