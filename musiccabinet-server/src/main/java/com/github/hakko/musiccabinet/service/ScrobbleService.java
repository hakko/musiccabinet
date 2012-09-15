package com.github.hakko.musiccabinet.service;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.joda.time.Seconds.secondsBetween;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.DateTime;
import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.message.GenericMessage;

import com.github.hakko.musiccabinet.dao.LastFmDao;
import com.github.hakko.musiccabinet.dao.PlayCountDao;
import com.github.hakko.musiccabinet.domain.model.aggr.Scrobble;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.service.lastfm.UpdateNowPlayingService;
import com.github.hakko.musiccabinet.ws.lastfm.ScrobbleClient;
import com.github.hakko.musiccabinet.ws.lastfm.UpdateNowPlayingClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

public class ScrobbleService {

	protected Map<LastFmUser, ConcurrentLinkedDeque<Scrobble>> userScrobbles = new HashMap<>();
	private List<Scrobble> failedScrobbles = new ArrayList<>();
	
	protected PollableChannel scrobbleChannel;
	protected UpdateNowPlayingClient nowPlayingClient;
	protected ScrobbleClient scrobbleClient;
	private LastFmDao lastFmDao;
	private PlayCountDao playCountDao;

	private AtomicBoolean started = new AtomicBoolean(false);
	
	 // minimum time (in sec) an element must have been on queue, to be considered as played
	private final static int MIN_TIME = 60 * 4;

	// songs that are shorter than MIN_TIME are considered as played if their length is at
	// least this long (in sec)
	private final static int MIN_DURATION = 30;

	private static final Logger LOG = Logger.getLogger(UpdateNowPlayingService.class);

	/* Async method that registers scrobbles and delegates submissions, in case last.fm is down. */
	public void scrobble(String lastFmUsername, Track track, boolean submission) {
		LastFmUser lastFmUser = lastFmDao.getLastFmUser(lastFmUsername);
		Scrobble scrobble = new Scrobble(lastFmUser, track, submission);
		
		scrobbleChannel.send(new GenericMessage<Scrobble>(scrobble));
		
		if (!started.getAndSet(true)) {
			startScrobblingService();
		}
	}

	@SuppressWarnings("unchecked")
	protected void receive() throws ApplicationException {
		Message<Scrobble> message;
		while ((message = (Message<Scrobble>) scrobbleChannel.receive()) != null) {
			LOG.debug("Try updating now playing.");
			Scrobble scrobble = message.getPayload();
			Scrobble previous = getPrevious(scrobble);
			LOG.debug("previous: " + previous + ", scrobble = " + scrobble);
			if (previous != null && tooClose(scrobble, previous) &&
					scrobble.getTrack().getId() == previous.getTrack().getId()) {
				LOG.debug("Same track was scrobbled just recently, ignore.");
			} else {
				addScrobble(scrobble);
				WSResponse wsResponse = nowPlayingClient.updateNowPlaying(scrobble);
				LOG.debug("Nowplaying successful: " + wsResponse.wasCallSuccessful());
				LOG.debug("Nowplaying response: " + wsResponse);
				if (!wsResponse.wasCallSuccessful()) {
					LOG.debug("Could not update now playing status at last.fm.");
				}
			}
		}
	}

	private Scrobble getPrevious(Scrobble scrobble) {
		LastFmUser lastFmUser = scrobble.getLastFmUser();
		if (userScrobbles.containsKey(lastFmUser)) {
			return userScrobbles.get(lastFmUser).peekLast();
		} else {
			userScrobbles.put(lastFmUser, new ConcurrentLinkedDeque<Scrobble>());
			return null;
		}
	}
	
	private void addScrobble(Scrobble scrobble) {
		ConcurrentLinkedDeque<Scrobble> deque = userScrobbles.get(scrobble.getLastFmUser());
		Scrobble tail;
		while ((tail = deque.peekLast()) != null && tooClose(tail, scrobble)) {
			// indicates the occurrence of a previous track that was played for a few
			// seconds, and that should be removed
			deque.pollLast();
		}
		deque.add(scrobble);
	}

	private boolean tooClose(Scrobble prev, Scrobble next) {
		return tooClose(prev, next.getStartTime());
	}

	private boolean tooClose(Scrobble prev, DateTime next) {
		int allowedDiff = max((prev.getTrack().getMetaData().getDuration() * 4) / 5, MIN_DURATION);
		allowedDiff = min(allowedDiff, MIN_TIME);
		LOG.debug("is " + prev.getStartTime() + " too close to " + next	+ "? diff = " + secondsBetween(prev.getStartTime(), next).getSeconds() + ", allowedDiff = " + allowedDiff);
		return secondsBetween(prev.getStartTime(), next).getSeconds() < allowedDiff;
	}

	protected void scrobbleTracks() throws ApplicationException {
		scrobbleFailedTracks();
		Scrobble head;
		for (LastFmUser lastFmUser : userScrobbles.keySet()) {
			ConcurrentLinkedDeque<Scrobble> deque = userScrobbles.get(lastFmUser);
			while ((head = deque.peekFirst()) != null && !tooClose(head, new DateTime())) {
				playCountDao.addPlayCount(head.getLastFmUser(), head.getTrack());
				WSResponse wsResponse = scrobbleClient.scrobble(head);
				LOG.debug("Scrobble successful: " + wsResponse.wasCallSuccessful());
				LOG.debug("Scrobble response: " + wsResponse);
				if (!wsResponse.wasCallSuccessful()) {
					LOG.warn("scrobbling " + head + " failed! Add for re-sending.");
					failedScrobbles.add(head);
				}
				deque.pollFirst();
			}
		}
	}

	protected void scrobbleFailedTracks() throws ApplicationException {
		while (failedScrobbles.size() > 0) {
			LOG.debug("Queue of failed scrobbles consists of " + failedScrobbles.size() + " elements.");
			Scrobble firstFailed = failedScrobbles.get(0);
			WSResponse wsResponse = scrobbleClient.scrobble(firstFailed);
			LOG.debug("Failed scrobble re-send successful: " + wsResponse.wasCallSuccessful());
			LOG.debug("Failed scrobble re-send response: " + wsResponse);
			if (wsResponse.wasCallSuccessful()) {
				LOG.debug("Failed scrobble was re-sent.");
				failedScrobbles.remove(0);
			} else {
				LOG.debug("Failed scrobble could not be re-sent. Wait a minute before trying again.");
				return;
			}
		}
	}
	
	protected void startScrobblingService() {
		
		ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
		threadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					receive();
				} catch (Throwable t) {
					LOG.error("Unexpected error caught while receiving scrobbles!", t);
				}
			}
		});
		
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					scrobbleTracks();
				} catch (Throwable t) {
					LOG.error("Unexpected error caught while scrobbling!", t);
				}
			}
		}, 1, 1, TimeUnit.MINUTES);
		
	}

	// Spring setters
	
	public void setUpdateNowPlayingClient(UpdateNowPlayingClient client) {
		this.nowPlayingClient = client;
	}

	public void setScrobbleClient(ScrobbleClient scrobbleClient) {
		this.scrobbleClient = scrobbleClient;
	}

	public void setScrobbleChannel(PollableChannel scrobbleChannel) {
		this.scrobbleChannel = scrobbleChannel;
	}

	public void setLastFmDao(LastFmDao lastFmDao) {
		this.lastFmDao = lastFmDao;
	}

	public void setPlayCountDao(PlayCountDao playCountDao) {
		this.playCountDao = playCountDao;
	}

}