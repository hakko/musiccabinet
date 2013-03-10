package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_USER_LOVED_TRACKS;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.UserLovedTracks;
import com.github.hakko.musiccabinet.domain.model.aggr.UserStarredTrack;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.UserLovedTracksParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcUserLovedTracksDaoTest {

	private static final String LOVED_TRACKS_FILE = 
			"last.fm/xml/userlovedtracks.rj.xml";

	private static final String USERNAME1 = "user1", USERNAME2 = "user2";
	private LastFmUser user1 = new LastFmUser(USERNAME1), 
			user2 = new LastFmUser(USERNAME2);

	private String artistName1 = "artist1", artistName2 = "artist2";
	private String albumName1 = "album1", albumName2 = "album2", albumName3 = "album3";
	private String trackName1 = "track1", trackName2 = "track2";

	private Album album1, album2;
	private Track track1, track2;

	@Autowired
	private JdbcUserLovedTracksDao dao;

	@Autowired
	private JdbcLibraryAdditionDao additionDao;

	@Autowired
	private JdbcStarDao starDao;

	@Autowired
	private JdbcLastFmDao lastFmDao;

	@Autowired
	private JdbcLibraryBrowserDao browserDao;

	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_USER_LOVED_TRACKS);
		PostgreSQLUtil.truncateTables(dao);

		lastFmDao.createOrUpdateLastFmUser(user1);
		lastFmDao.createOrUpdateLastFmUser(user2);

		File file1 = getFile(artistName1, albumName1, trackName1), 
				file2 = getFile(artistName2, albumName2, trackName2);
		submitFile(additionDao, Arrays.asList(file1, file2));

		List<Artist> artists = browserDao.getArtists();
		assertEquals(2, artists.size());
		album1 = browserDao.getAlbums(artists.get(0).getId(), true).get(0);
		album2 = browserDao.getAlbums(artists.get(1).getId(), true).get(0);
		
		track1 = browserDao.getTracks(album1.getTrackIds()).get(0);
		track2 = browserDao.getTracks(album2.getTrackIds()).get(0);

		deleteLovedAndStarredTracks();
	}
	
	@Test
	public void createAndRetrievesUserLovedTracks() throws ApplicationException {
		List<Track> lovedTracks = new UserLovedTracksParserImpl(new ResourceUtil(
				LOVED_TRACKS_FILE).getInputStream()).getLovedTracks();
		dao.createLovedTracks(asList(new UserLovedTracks(USERNAME1, lovedTracks)));
		List<Track> dbLovedTracks = dao.getLovedTracks(USERNAME1);
		
		Collections.sort(lovedTracks);
		Collections.sort(dbLovedTracks);
		
		assertEquals(lovedTracks, dbLovedTracks);
	}

	@Test
	public void deletesUnlovedTracksIdentifiedInImport() throws ApplicationException {
		List<Track> lovedTracks = new UserLovedTracksParserImpl(new ResourceUtil(
				LOVED_TRACKS_FILE).getInputStream()).getLovedTracks();
		dao.createLovedTracks(asList(new UserLovedTracks(USERNAME1, lovedTracks)));

		lovedTracks.remove(0);
		dao.createLovedTracks(asList(new UserLovedTracks(USERNAME1, lovedTracks)));
		
		List<Track> dbLovedTracks = dao.getLovedTracks(USERNAME1);
		Collections.sort(lovedTracks);
		Collections.sort(dbLovedTracks);
		
		assertEquals(lovedTracks, dbLovedTracks);
	}
	
	@Test
	public void addsAndDeletesLovedTracksSeparatelyPerUsers() {
		dao.createLovedTracks(asList(
				new UserLovedTracks(USERNAME1, asList(track1)),
				new UserLovedTracks(USERNAME2, asList(track2))));
		
		assertEquals(asList(track1), dao.getLovedTracks(USERNAME1));
		assertEquals(asList(track2), dao.getLovedTracks(USERNAME2));

		dao.createLovedTracks(asList(
				new UserLovedTracks(USERNAME1, asList(track1)),
				new UserLovedTracks(USERNAME2, asList(track1, track2))));
		
		assertEquals(asList(track1), dao.getLovedTracks(USERNAME1));
		assertEquals(asList(track1, track2), sort(dao.getLovedTracks(USERNAME2)));
	}

	private List<Track> sort(List<Track> tracks) {
		Collections.sort(tracks);
		return tracks;
	}

	@Test
	public void addsStarredTrackWhenImportingNewLovedTracks() {
		starDao.starTrack(user1, track1.getId());
		starDao.starTrack(user2, track2.getId());
		
		dao.createLovedTracks(asList(new UserLovedTracks(USERNAME1, asList(track2))));
		
		List<Integer> starred1 = starDao.getStarredTrackIds(user1, 0, 10, null);
		List<Integer> starred2 = starDao.getStarredTrackIds(user2, 0, 10, null);
		
		assertEquals(Arrays.asList(track2.getId(), track1.getId()), starred1);
		assertEquals(Arrays.asList(track2.getId()), starred2);
	}

	@Test
	public void removesStarredTracksWhenPreviouslyLovedTrackIsMissingInImport() {
		starDao.starTrack(user1, track1.getId());
		starDao.starTrack(user2, track2.getId());

		dao.createLovedTracks(asList(
				new UserLovedTracks(USERNAME1, asList(track1, track2)),
				new UserLovedTracks(USERNAME2, asList(track2))));
		dao.createLovedTracks(asList(
				new UserLovedTracks(USERNAME1, asList(track2)),
				new UserLovedTracks(USERNAME2, asList(track2))));

		List<Integer> starred1 = starDao.getStarredTrackIds(user1, 0, 10, null);
		List<Integer> starred2 = starDao.getStarredTrackIds(user2, 0, 10, null);

		assertEquals(Arrays.asList(track2.getId()), starred1);
		assertEquals(Arrays.asList(track2.getId()), starred2);
	}

	@Test
	public void starredTracksAreOnlyRemovedIfPreviouslyDefinedAsLovedTrack() {
		starDao.starTrack(user1, track1.getId());
		starDao.starTrack(user1, track2.getId());

		dao.createLovedTracks(asList(
				new UserLovedTracks(USERNAME1, asList(track2)),
				new UserLovedTracks(USERNAME2, asList(track2))));
		
		List<Integer> starred1 = starDao.getStarredTrackIds(user1, 0, 10, null);
		List<Integer> starred2 = starDao.getStarredTrackIds(user2, 0, 10, null);

		assertEquals(asList(track2.getId(), track1.getId()), starred1);
		assertEquals(asList(track2.getId()), starred2);

		dao.createLovedTracks(asList(
				new UserLovedTracks(USERNAME1, asList(track1)),
				new UserLovedTracks(USERNAME2, asList(track2))));

		starred1 = starDao.getStarredTrackIds(user1, 0, 10, null);
		starred2 = starDao.getStarredTrackIds(user2, 0, 10, null);

		assertEquals(asList(track1.getId()), starred1);
		assertEquals(asList(track2.getId()), starred2);
	}

	@Test
	public void identifiesStarredButNotLovedTracks() {
		starDao.starTrack(user1, track1.getId());
		starDao.starTrack(user2, track1.getId());

		dao.createLovedTracks(asList(new UserLovedTracks(USERNAME1, asList(track1, track2))));

		List<UserStarredTrack> userStarredTracks = dao.getStarredButNotLovedTracks();
		assertEquals(1, userStarredTracks.size());
		assertEquals(user2, userStarredTracks.get(0).getLastFmUser());
		assertEquals(track1, userStarredTracks.get(0).getStarredTrack());
	}

	@Test
	public void onlyTracksFromEarliestAlbumsAreStarredWhenImportingLovedTracks() {
		File f1, f2, f3, f4, f5;
		deleteLovedAndStarredTracks();
		additionDao.getJdbcTemplate().execute("truncate library.directory cascade");
		submitFile(additionDao, asList(
				f1 = getFile(artistName1, albumName1, trackName1, (short) 2006),
				f2 = getFile(artistName1, albumName2, trackName1, (short) 1999),
				f3 = getFile(artistName2, albumName1, trackName2, (short) 2010),
				f4 = getFile(artistName2, albumName2, trackName2, (short) 2011),
				f5 = getFile(artistName2, albumName3, trackName2, (short) 2012)));

		dao.createLovedTracks(asList(
				new UserLovedTracks(USERNAME1, asList(new Track(artistName1, trackName1))),
				new UserLovedTracks(USERNAME2, asList(new Track(artistName2, trackName2)))));

		assertFalse(browserDao.getTrackId(f1) == browserDao.getTrackId(f2));
		assertFalse(browserDao.getTrackId(f3) == browserDao.getTrackId(f4));
		assertFalse(browserDao.getTrackId(f3) == browserDao.getTrackId(f5));

		assertEquals(asList(browserDao.getTrackId(f2)),
				starDao.getStarredTrackIds(lastFmDao.getLastFmUser(USERNAME1)));
		assertEquals(asList(browserDao.getTrackId(f3)),
				starDao.getStarredTrackIds(lastFmDao.getLastFmUser(USERNAME2)));
	}

	private void deleteLovedAndStarredTracks() {
		dao.getJdbcTemplate().execute("truncate music.lovedtrack");
		dao.getJdbcTemplate().execute("truncate library.starredtrack");
	}

}