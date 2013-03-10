package com.github.hakko.musiccabinet.service.lastfm;

import static com.github.hakko.musiccabinet.configuration.CharSet.UTF8;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcLastFmDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryAdditionDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryBrowserDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcStarDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcUserLovedTracksDao;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.StarService;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.ws.lastfm.TrackLoveClient;
import com.github.hakko.musiccabinet.ws.lastfm.UserLovedTracksClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Also see @JdbcUserLovedTracksDaoTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class UserLovedTracksServiceTest {

	@Autowired
	private UserLovedTracksService service;

	@Autowired
	private JdbcLastFmDao lastFmDao;

	@Autowired
	private JdbcUserLovedTracksDao userLovedTracksDao;

	@Autowired
	private JdbcStarDao starDao;

	@Autowired
	private JdbcLibraryBrowserDao browserDao;

	@Autowired
	private JdbcLibraryAdditionDao additionDao;

	private static final String LOVED_TRACKS_FILE =
			"last.fm/xml/userlovedtracks.singlepage.xml";
	private static final String USER1 = "user1", USER2 = "user2";

	@Test
	public void serviceConfigured() {
		assertNotNull(service);
		assertNotNull(service.webserviceHistoryService);
		assertNotNull(service.lastFmSettingsService);
		assertNotNull(service.userLovedTracksClient);
		assertNotNull(service.userLovedTracksDao);
		assertNotNull(service.trackLoveClient);
		assertNotNull(service.starService);
	}

	/*
	 * User story: user1 has starred track2, user2 has starred track3 & track4.
	 * When importing loved tracks, user2 is unavailable and user1 has loved track1.
	 * Expected outcome: afterwards, user1 has starred track1 & 2 and user2 track3 & 4.
	 */
	@Test
	public void updatesStarredTracksWhenImportIsOnlyAllowedForOneUser() throws ApplicationException {
		lastFmDao.createOrUpdateLastFmUser(new LastFmUser(USER1));
		lastFmDao.createOrUpdateLastFmUser(new LastFmUser(USER2));
		LastFmUser user1 = lastFmDao.getLastFmUser(USER1),
				user2 = lastFmDao.getLastFmUser(USER2);
		Track track1 = new Track("Frank Ocean", "Lost"),
				track2 = new Track("Kate Bush", "Cloudbusting"),
				track3 = new Track("Adele", "Skyfall"),
				track4 = new Track("Kath Bloom", "Fall Again");
		File f1, f2, f3, f4;

		deleteLovedAndStarredTracks();
		submitFile(additionDao, asList(f1 = getFile(track1), f2 = getFile(track2),
				f3 = getFile(track3), f4 = getFile(track4)));
		int track1Id = browserDao.getTrackId(f1),
				track2Id = browserDao.getTrackId(f2),
				track3Id = browserDao.getTrackId(f3),
				track4Id = browserDao.getTrackId(f4);

		LastFmSettingsService lastFmSettingsService = mock(LastFmSettingsService.class);
		when(lastFmSettingsService.getLastFmUsers()).thenReturn(asList(user1, user2));
		when(lastFmSettingsService.isSyncStarredAndLovedTracks()).thenReturn(true);

		UserLovedTracksClient userLovedTracksClient = mock(UserLovedTracksClient.class);
		when(userLovedTracksClient.getUserLovedTracks(user1, (short) 0)).thenReturn(
				new WSResponse(new ResourceUtil(LOVED_TRACKS_FILE, UTF8).getContent()));
		when(userLovedTracksClient.getUserLovedTracks(user2, (short) 0)).thenReturn(
				new WSResponse(false, 403, "Forbidden"));

		UserLovedTracksService userLovedTracksService = new UserLovedTracksService();
		userLovedTracksService.setLastFmSettingsService(lastFmSettingsService);
		userLovedTracksService.setUserLovedTracksClient(userLovedTracksClient);
		userLovedTracksService.setUserLovedTracksDao(userLovedTracksDao);
		userLovedTracksService.setStarService(mock(StarService.class));
		userLovedTracksService.setTrackLoveClient(mock(TrackLoveClient.class));

		starDao.starTrack(user1, track2Id);
		starDao.starTrack(user2, track3Id);
		starDao.starTrack(user2, track4Id);
		userLovedTracksService.updateSearchIndex();

		assertEquals(sort(asList(track1Id, track2Id)), sort(starDao.getStarredTrackIds(user1)));
		assertEquals(sort(asList(track3Id, track4Id)), sort(starDao.getStarredTrackIds(user2)));
	}

	private List<Integer> sort(List<Integer> trackIds) {
		Collections.sort(trackIds);
		return trackIds;
	}

	private void deleteLovedAndStarredTracks() {
		additionDao.getJdbcTemplate().execute("truncate library.directory cascade");
		userLovedTracksDao.getJdbcTemplate().execute("truncate music.lovedtrack");
		userLovedTracksDao.getJdbcTemplate().execute("truncate library.starredtrack");
	}

}