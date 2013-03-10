package com.github.hakko.musiccabinet.service.lastfm;

import static com.github.hakko.musiccabinet.configuration.CharSet.UTF8;
import static com.github.hakko.musiccabinet.domain.model.library.Period.OVERALL;
import static com.github.hakko.musiccabinet.domain.model.library.Period.SIX_MONTHS;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcArtistInfoDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcLastFmDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryAdditionDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcUserTopArtistsDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.UserTopArtists;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.library.Period;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.UserTopArtistsParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;
import com.github.hakko.musiccabinet.ws.lastfm.UserTopArtistsClient;
import com.github.hakko.musiccabinet.ws.lastfm.WSResponse;

/*
 * Also see @JdbcUserTopArtistsDaoTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class UserTopArtistsServiceTest {

	@Autowired
	private UserTopArtistsService service;

	@Autowired
	private JdbcLastFmDao lastFmDao;

	@Autowired
	private JdbcUserTopArtistsDao userTopArtistsDao;

	@Autowired
	private JdbcArtistInfoDao artistInfoDao;

	@Autowired
	private JdbcLibraryAdditionDao additionDao;

	private static final String USER1 = "user1", USER2 = "user2";
	private static final String TOP_ARTISTS_FILE =
			"last.fm/xml/usertopartists.singlepage.%s.xml";

	@Test
	public void serviceConfigured() {
		assertNotNull(service);
		assertNotNull(service.userTopArtistsClient);
		assertNotNull(service.userTopArtistsDao);
		assertNotNull(service.webserviceHistoryService);
		assertNotNull(service.lastFmSettingsService);
	}

	/*
	 * User story: user1 and user2 have (previously imported) top artists.
	 * During import, new data can only be fetched for user1.
	 * Expected: top artists for user1 are updated, while user2 keeps original artists.
	 */
	@Test
	public void updatesTopArtistsWhenImportIsOnlyAllowedForOneUser() throws Exception {
		PostgreSQLUtil.truncateTables(userTopArtistsDao);

		createArtistInfosAndLocalFiles();
		lastFmDao.createOrUpdateLastFmUser(new LastFmUser(USER1));
		lastFmDao.createOrUpdateLastFmUser(new LastFmUser(USER2));
		LastFmUser user1 = lastFmDao.getLastFmUser(USER1),
				user2 = lastFmDao.getLastFmUser(USER2);

		LastFmSettingsService lastFmSettingsService = mock(LastFmSettingsService.class);
		when(lastFmSettingsService.getLastFmUsers()).thenReturn(asList(user1, user2));

		UserTopArtistsClient userTopArtistsClient = mock(UserTopArtistsClient.class);
		for (Period period : Period.values()) {
			String fileName = format(TOP_ARTISTS_FILE, period.getDescription());
			when(userTopArtistsClient.getUserTopArtists(user1, period)).thenReturn(
					new WSResponse(new ResourceUtil(fileName, UTF8).getContent()));
			when(userTopArtistsClient.getUserTopArtists(user2, period)).thenReturn(
					new WSResponse(false, 403, "Forbidden"));
		}

		UserTopArtistsService userTopArtistsService = new UserTopArtistsService();
		userTopArtistsService.setLastFmSettingsService(lastFmSettingsService);
		userTopArtistsService.setUserTopArtistsClient(userTopArtistsClient);
		userTopArtistsService.setUserTopArtistsDao(userTopArtistsDao);

		userTopArtistsDao.createUserTopArtists(asList(
				new UserTopArtists(user1, OVERALL, asList(new Artist("M83"))),
				new UserTopArtists(user2, SIX_MONTHS, asList(new Artist("Zola Jesus")))));
		assertEquals("M83", userTopArtistsService.getUserTopArtists(user1, OVERALL, 0, 10)
				.get(0).getArtistName());
		assertEquals("Zola Jesus", userTopArtistsService.getUserTopArtists(user2, SIX_MONTHS, 0, 10)
				.get(0).getArtistName());

		userTopArtistsService.updateSearchIndex();
		assertEquals(1, userTopArtistsService.getUserTopArtists(user2, SIX_MONTHS, 0, 10).size());
		for (Period period : Period.values()) {
			assertEquals("Expected 50 artists for period " + period, 50,
					userTopArtistsService.getUserTopArtists(user1, period, 0, 50).size());
		}
	}

	private void createArtistInfosAndLocalFiles() throws ApplicationException {
		Map<Artist, ArtistInfo> artistInfos = new HashMap<>();
		List<File> files = new ArrayList<>();
		for (Period period : Period.values()) {
			String fileName = format(TOP_ARTISTS_FILE, period.getDescription());
			for (Artist artist : new UserTopArtistsParserImpl(
					new ResourceUtil(fileName, UTF8).getInputStream()).getArtists()) {
				artistInfos.put(artist, new ArtistInfo(artist));
				files.add(UnittestLibraryUtil.getFile(artist.getName(), "A", "T"));
			}
		}
		artistInfoDao.createArtistInfo(new ArrayList<ArtistInfo>(artistInfos.values()));
		submitFile(additionDao, files);
	}
}