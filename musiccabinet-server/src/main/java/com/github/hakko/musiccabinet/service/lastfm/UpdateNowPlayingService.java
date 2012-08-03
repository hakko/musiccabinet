package com.github.hakko.musiccabinet.service.lastfm;

import static com.github.hakko.musiccabinet.service.library.LibraryUtil.FINISHED_MESSAGE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.joda.time.Seconds.secondsBetween;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;

import com.github.hakko.musiccabinet.domain.model.aggr.Scrobble;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.ws.lastfm.UpdateNowPlayingClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

public class UpdateNowPlayingService implements InitializingBean {
	
	protected Map<LastFmUser, ConcurrentLinkedDeque<Scrobble>> userScrobbles = new HashMap<>();

	protected TaskExecutor taskExecutor;
	protected PollableChannel scrobbleChannel;
	private UpdateNowPlayingClient client;

	 // minimum time (in sec) an element must have been on queue, to be considered as played
	private final static int MIN_TIME = 60;

	// songs that are shorter than MIN_TIME are considered as played if their length is at
	// least this long (in sec)
	private final static int MIN_DURATION = 30;

	private static final Logger LOG = Logger.getLogger(UpdateNowPlayingService.class);

	@SuppressWarnings("unchecked")
	public void receive() {
		while (true) {
			Message<Scrobble> message = (Message<Scrobble>) scrobbleChannel.receive();
			if (message == null || message.equals(FINISHED_MESSAGE)) {
				break;
			} else {
				try {
					LOG.debug("Try updating now playing.");
					Scrobble scrobble = message.getPayload();
					Scrobble previous = getPrevious(scrobble);
					LOG.debug("previous: " + previous + ", scrobble = " + scrobble);
					if (previous != null && tooClose(scrobble, previous) &&
						scrobble.getTrack().getId() == previous.getTrack().getId()) {
						LOG.debug("Same track was scrobbled just recently, ignore.");
					} else {
						addScrobble(scrobble);
						WSResponse wsResponse = client.updateNowPlaying(message.getPayload());
						LOG.debug("Successful: " + wsResponse.wasCallSuccessful());
						LOG.debug("Response: " + wsResponse.getResponseBody());
					}
				} catch (ApplicationException e) {
					LOG.warn("Could not update now playing at last.fm.", e);
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
		int allowedDiff = max(prev.getTrack().getMetaData().getDuration(), MIN_DURATION);
		allowedDiff = min(allowedDiff, MIN_TIME);
		return secondsBetween(prev.getStartTime(), next).getSeconds() < allowedDiff;
	}

	private void scrobbleTracks() {
		Scrobble head;
		for (LastFmUser lastFmUser : userScrobbles.keySet()) {
			ConcurrentLinkedDeque<Scrobble> deque = userScrobbles.get(lastFmUser);
			while ((head = deque.peekFirst()) != null) {
				if (!tooClose(head, new DateTime())) {
					// update play stats
					// scrobble
					deque.pollFirst();
				}
			}
		}
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
	
	public void setScrobbleChannel(PollableChannel scrobbleChannel) {
		this.scrobbleChannel = scrobbleChannel;
	}

	public void setUpdateNowPlayingClient(UpdateNowPlayingClient client) {
		this.client = client;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		taskExecutor.execute(new Runnable() {
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

}