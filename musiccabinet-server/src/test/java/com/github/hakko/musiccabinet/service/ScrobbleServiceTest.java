package com.github.hakko.musiccabinet.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.integration.Message;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.message.GenericMessage;

import com.github.hakko.musiccabinet.dao.PlayCountDao;
import com.github.hakko.musiccabinet.domain.model.aggr.Scrobble;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.library.MetaData;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.ws.lastfm.ScrobbleClient;
import com.github.hakko.musiccabinet.ws.lastfm.UpdateNowPlayingClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

public class ScrobbleServiceTest {

	private ScrobbleService scrobbleService;
	
	private String username1 = "user1", username2 = "user2";
	private String sessionKey1 = "sessionKey1", sessionKey2 = "sessionKey2";
	private LastFmUser user1, user2;
	private int artist1Id = 1;
	private int album1Id = 1;
	private int track1Id = 1, track2Id;
	private Track track1, track2;
	
	@Before
	public void createTestData() throws ApplicationException {

		scrobbleService = new ScrobbleService();

		UpdateNowPlayingClient nowPlayinglient = mock(UpdateNowPlayingClient.class);
		when(nowPlayinglient.updateNowPlaying(Mockito.any(Scrobble.class))).thenReturn(
				new WSResponse(false, 404, "Not found"));
		scrobbleService.setUpdateNowPlayingClient(nowPlayinglient);

		ScrobbleClient scrobbleClient = mock(ScrobbleClient.class);
		when(scrobbleClient.scrobble(Mockito.any(Scrobble.class))).thenReturn(
				new WSResponse("<lfm status=\"ok\"></lfm>"));
		scrobbleService.setScrobbleClient(scrobbleClient);
		
		PlayCountDao playCountDao = mock(PlayCountDao.class);
		scrobbleService.setPlayCountDao(playCountDao);
		
		MetaData metaData1 = new MetaData();
		metaData1.setArtist("artist 1");
		metaData1.setArtistId(artist1Id);
		metaData1.setAlbum("album 1");
		metaData1.setAlbumId(album1Id);
		track1 = new Track(track1Id, "track 1", metaData1);
		track2 = new Track(track2Id, "track 2", metaData1);
		user1 = new LastFmUser(username1, sessionKey1);
		user2 = new LastFmUser(username2, sessionKey2);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void skipsIdenticalScrobble() throws InterruptedException, ApplicationException {
		
		// a second scrobble of the same track is just silently thrown away.
		// verify by checking that it's the original scrobble (check startTime)
		// that remains in the queue when a dupe is scrobbled.
		
		Scrobble scrobble1 = new Scrobble(user1, track1, false);
		Thread.sleep(3);
		Scrobble scrobble2 = new Scrobble(user1, track1, false);

		Message message1 = new GenericMessage<Scrobble>(scrobble1);
		Message message2 = new GenericMessage<Scrobble>(scrobble2);
		PollableChannel scrobbleChannel = mock(PollableChannel.class);
		when(scrobbleChannel.receive()).thenReturn(message1, message2, null);
		
		scrobbleService.setScrobbleChannel(scrobbleChannel);
		scrobbleService.receive();
		
		assertNotNull(scrobbleService.userScrobbles);
		assertEquals(1, scrobbleService.userScrobbles.keySet().size());
		assertEquals(1, scrobbleService.userScrobbles.get(scrobble1.getLastFmUser()).size());
		assertEquals(scrobble1.getStartTime(), scrobbleService.userScrobbles
				.get(scrobble1.getLastFmUser()).getFirst().getStartTime());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void previousScrobbleGetsRemovedOnImmediateOtherScrobble() throws InterruptedException, ApplicationException {

		// differently from test case above, a close subsequent scan of a
		// new track removes the previous scrobble from the queue (not the new).
		
		Scrobble scrobble1 = new Scrobble(user1, track1, false);
		Thread.sleep(3);
		Scrobble scrobble2 = new Scrobble(user1, track2, false);

		assertFalse(scrobble1.getStartTime().equals(scrobble2.getStartTime()));
		
		Message message1 = new GenericMessage<Scrobble>(scrobble1);
		Message message2 = new GenericMessage<Scrobble>(scrobble2);
		PollableChannel scrobbleChannel = mock(PollableChannel.class);
		when(scrobbleChannel.receive()).thenReturn(message1, message2, null);
		
		scrobbleService.setScrobbleChannel(scrobbleChannel);
		scrobbleService.receive();
		
		assertNotNull(scrobbleService.userScrobbles);
		assertEquals(1, scrobbleService.userScrobbles.keySet().size());
		assertEquals(1, scrobbleService.userScrobbles.get(scrobble1.getLastFmUser()).size());
		assertEquals(track2Id, scrobbleService.userScrobbles.get(scrobble1.getLastFmUser())
				.getFirst().getTrack().getId());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void differentUsersCanScrobbleSameTrack() throws ApplicationException {
		
		Scrobble scrobble1 = new Scrobble(user1, track1, false);
		Scrobble scrobble2 = new Scrobble(user2, track1, false);

		Message message1 = new GenericMessage<Scrobble>(scrobble1);
		Message message2 = new GenericMessage<Scrobble>(scrobble2);
		PollableChannel scrobbleChannel = mock(PollableChannel.class);
		when(scrobbleChannel.receive()).thenReturn(message1, message2, null);
		
		scrobbleService.setScrobbleChannel(scrobbleChannel);
		scrobbleService.receive();
		
		assertNotNull(scrobbleService.userScrobbles);
		assertEquals(2, scrobbleService.userScrobbles.keySet().size());
		assertEquals(1, scrobbleService.userScrobbles.get(scrobble1.getLastFmUser()).size());
		assertEquals(1, scrobbleService.userScrobbles.get(scrobble2.getLastFmUser()).size());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void scrobblerIgnoresTooNewSubmissions() throws ApplicationException {

		Scrobble scrobble = new Scrobble(user1, track1, false);

		Message message = new GenericMessage<Scrobble>(scrobble);
		PollableChannel scrobbleChannel = mock(PollableChannel.class);
		when(scrobbleChannel.receive()).thenReturn(message, (Message) null);

		scrobbleService.setScrobbleChannel(scrobbleChannel);
		scrobbleService.receive();

		scrobbleService.scrobbleTracks();
		assertNotNull(scrobbleService.userScrobbles);
		assertEquals(1, scrobbleService.userScrobbles.keySet().size());
		assertEquals(1, scrobbleService.userScrobbles.get(scrobble.getLastFmUser()).size());
		
		scrobble.setStartTime(scrobble.getStartTime().minusSeconds(10));
		scrobbleService.scrobbleTracks();
		assertEquals(1, scrobbleService.userScrobbles.get(scrobble.getLastFmUser()).size());

		scrobble.setStartTime(scrobble.getStartTime().minusMinutes(10));
		scrobbleService.scrobbleTracks();
		assertEquals(0, scrobbleService.userScrobbles.get(scrobble.getLastFmUser()).size());
	}
	
}