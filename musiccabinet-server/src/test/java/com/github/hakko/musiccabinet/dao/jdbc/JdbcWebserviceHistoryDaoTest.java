package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.GET_ARTIST_ID;
import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.GET_TRACK_ID;
import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.GET_LASTFMUSER_ID;
import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.GET_LASTFMGROUP_ID;
import static com.github.hakko.musiccabinet.domain.model.library.Period.OVERALL;
import static com.github.hakko.musiccabinet.domain.model.library.Period.SIX_MONTHS;
import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.ARTIST_GET_INFO;
import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.ARTIST_GET_TOP_TRACKS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.LastFmGroup;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation;
import com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Tag;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcWebserviceHistoryDaoTest {

	@Autowired
	private JdbcWebserviceHistoryDao dao;

	@Autowired
	private JdbcMusicDao musicDao;

	@Autowired 
	private JdbcTagDao tagDao;
	
	@Autowired
	private JdbcLibraryAdditionDao additionDao;
	
	@Before
	public void setUp() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, GET_ARTIST_ID);
		PostgreSQLUtil.loadFunction(dao, GET_TRACK_ID);
		PostgreSQLUtil.loadFunction(dao, GET_LASTFMUSER_ID);
		PostgreSQLUtil.loadFunction(dao, GET_LASTFMGROUP_ID);
	}
	
	@Test
	public void importPageFromPersonLibraryNotPossibleTwice() {
		Calltype LIB = Calltype.GET_SCROBBLED_TRACKS;
		short page3 = 3;
		short page4 = 4;
		WebserviceInvocation libPage3 = new WebserviceInvocation(LIB, page3);
		WebserviceInvocation libPage4 = new WebserviceInvocation(LIB, page4);

		deleteWebserviceInvocations();

		assertTrue(dao.isWebserviceInvocationAllowed(libPage3));
		assertTrue(dao.isWebserviceInvocationAllowed(libPage4));
		dao.logWebserviceInvocation(libPage4);
		assertTrue(dao.isWebserviceInvocationAllowed(libPage3));
		assertFalse(dao.isWebserviceInvocationAllowed(libPage4));
		dao.logWebserviceInvocation(libPage3);
		assertFalse(dao.isWebserviceInvocationAllowed(libPage3));
		assertFalse(dao.isWebserviceInvocationAllowed(libPage4));
	}

	@Test
	public void importArtistTopTracksNotPossibleTwice() {
		Calltype TOP = Calltype.ARTIST_GET_TOP_TRACKS;
		Artist artist1 = new Artist("Esben And The Witch");
		Artist artist2 = new Artist("Espers");
		Artist artist3 = new Artist("Essie Jain");
		WebserviceInvocation topArtist1 = new WebserviceInvocation(TOP, artist1);
		WebserviceInvocation topArtist2 = new WebserviceInvocation(TOP, artist2);
		WebserviceInvocation topArtist3 = new WebserviceInvocation(TOP, artist3);
		
		deleteWebserviceInvocations();
		
		// possible for all of them
		assertTrue(dao.isWebserviceInvocationAllowed(topArtist1));
		assertTrue(dao.isWebserviceInvocationAllowed(topArtist2));
		assertTrue(dao.isWebserviceInvocationAllowed(topArtist3));
		// invoke artist1
		dao.logWebserviceInvocation(topArtist1);
		// now possible for all of them but artist1
		assertFalse(dao.isWebserviceInvocationAllowed(topArtist1));
		assertTrue(dao.isWebserviceInvocationAllowed(topArtist2));
		assertTrue(dao.isWebserviceInvocationAllowed(topArtist3));

		// invoke artist2 & artist3
		dao.logWebserviceInvocation(topArtist2);
		dao.logWebserviceInvocation(topArtist3);
		// now none of them can be invoked
		assertFalse(dao.isWebserviceInvocationAllowed(topArtist1));
		assertFalse(dao.isWebserviceInvocationAllowed(topArtist2));
		assertFalse(dao.isWebserviceInvocationAllowed(topArtist3));
	}
	
	@Test
	public void webserviceHistoryIsCaseInsensitive() {
		Calltype TOP = Calltype.ARTIST_GET_TOP_TRACKS;
		Artist artist1 = new Artist("Håll Det Äkta");
		Artist artist2 = new Artist("HÅLL DET ÄKTA");
		WebserviceInvocation topArtist1 = new WebserviceInvocation(TOP, artist1);
		WebserviceInvocation topArtist2 = new WebserviceInvocation(TOP, artist2);
		
		deleteWebserviceInvocations();

		assertTrue(dao.isWebserviceInvocationAllowed(topArtist1));
		assertTrue(dao.isWebserviceInvocationAllowed(topArtist2));

		dao.logWebserviceInvocation(topArtist1);

		assertFalse(dao.isWebserviceInvocationAllowed(topArtist1));
		assertFalse(dao.isWebserviceInvocationAllowed(topArtist2));
	}
	
	@Test
	public void importArtistSimilaritiesAndTopTracksWorkIndependently() {
		Calltype TOP = Calltype.ARTIST_GET_TOP_TRACKS;
		Calltype SIMILAR = Calltype.ARTIST_GET_SIMILAR;
		Artist artist1 = new Artist("Björk");
		Artist artist2 = new Artist("Björn Olsson");

		WebserviceInvocation topArtist1 = new WebserviceInvocation(TOP, artist1);
		WebserviceInvocation topArtist2 = new WebserviceInvocation(TOP, artist2);
		WebserviceInvocation similarArtist1 = new WebserviceInvocation(SIMILAR, artist1);
		WebserviceInvocation similarArtist2 = new WebserviceInvocation(SIMILAR, artist2);

		deleteWebserviceInvocations();

		// everything's allowed
		assertTrue(dao.isWebserviceInvocationAllowed(topArtist1));
		assertTrue(dao.isWebserviceInvocationAllowed(similarArtist1));

		// invoke SIMILAR for artist1, TOP is still allowed
		dao.logWebserviceInvocation(similarArtist1);
		assertTrue(dao.isWebserviceInvocationAllowed(topArtist1));
		assertFalse(dao.isWebserviceInvocationAllowed(similarArtist1));

		// invoke TOP for artist2, SIMILAR is still allowed
		dao.logWebserviceInvocation(topArtist2);
		assertFalse(dao.isWebserviceInvocationAllowed(topArtist2));
		assertTrue(dao.isWebserviceInvocationAllowed(similarArtist2));
	}
	
	@Test
	public void importTrackSimilaritiesIsNotPossibleTwice() {
		Calltype SIMILAR = Calltype.TRACK_GET_SIMILAR;
		Artist artist = new Artist("Bill Fay");
		Track track1 = new Track(artist, "Omega");
		Track track2 = new Track(artist, "Don't let my marigolds die");
		
		WebserviceInvocation similarTrack1 = new WebserviceInvocation(SIMILAR, track1);
		WebserviceInvocation similarTrack2 = new WebserviceInvocation(SIMILAR, track2);

		deleteWebserviceInvocations();
		
		assertTrue(dao.isWebserviceInvocationAllowed(similarTrack1));
		assertTrue(dao.isWebserviceInvocationAllowed(similarTrack2));
		dao.logWebserviceInvocation(similarTrack2);
		assertTrue(dao.isWebserviceInvocationAllowed(similarTrack1));
		assertFalse(dao.isWebserviceInvocationAllowed(similarTrack2));
	}
	
	@Test
	public void importTrackSimilaritiesIsPossibleForCovers() {
		Calltype SIMILAR = Calltype.TRACK_GET_SIMILAR;
		Track track1 = new Track("Daniel Johnston", "True Love Will Find You In The End");
		Track track2 = new Track("Headless Heroes", "True Love Will Find You In The End");

		WebserviceInvocation similarTrack1 = new WebserviceInvocation(SIMILAR, track1);
		WebserviceInvocation similarTrack2 = new WebserviceInvocation(SIMILAR, track2);
		
		deleteWebserviceInvocations();

		assertTrue(dao.isWebserviceInvocationAllowed(similarTrack1));
		assertTrue(dao.isWebserviceInvocationAllowed(similarTrack2));
		dao.logWebserviceInvocation(similarTrack1);
		assertFalse(dao.isWebserviceInvocationAllowed(similarTrack1));
		assertTrue(dao.isWebserviceInvocationAllowed(similarTrack2));
		dao.logWebserviceInvocation(similarTrack2);
		assertFalse(dao.isWebserviceInvocationAllowed(similarTrack1));
		assertFalse(dao.isWebserviceInvocationAllowed(similarTrack2));
	}

	@Test
	public void oldInvocationsAreIgnored() {
		Calltype SIMILAR = Calltype.TRACK_GET_SIMILAR;
		Track track = new Track("Red Sparowes", "Finally, As That Blazing Sun Shone Down Upon Us, Did We Know That True Enemy Was the Voice of Blind Idolatry; and Only Then Did We Begin to Think for Ourselves.");
		WebserviceInvocation similarTrack = new WebserviceInvocation(SIMILAR, track);

		deleteWebserviceInvocations();

		assertTrue(dao.isWebserviceInvocationAllowed(similarTrack));
		dao.logWebserviceInvocation(similarTrack);
		assertFalse(dao.isWebserviceInvocationAllowed(similarTrack));

		// make the invocation age in database, by updating it.
		DateTime dateTime = new DateTime();
		JdbcTemplate jdbcTemplate = dao.getJdbcTemplate();
		for (int days = 1; days <= 14; days++) {
			jdbcTemplate.update("update library.webservice_history set invocation_time = ?", 
					new Object[]{dateTime.minusDays(days).toDate()});
			boolean isAllowed = dao.isWebserviceInvocationAllowed(similarTrack);
			boolean cacheIsInvalid = days > SIMILAR.getDaysToCache();
			assertTrue(isAllowed == cacheIsInvalid);
		}
	}

	@Test
	public void importUserRelatedDataNotPossibleTwice() {
		Calltype TOP_ARTISTS = Calltype.USER_GET_TOP_ARTISTS;
		LastFmUser user = new LastFmUser("arnathalon");
		WebserviceInvocation topArtists = new WebserviceInvocation(TOP_ARTISTS, user, OVERALL.getDays());
		
		deleteWebserviceInvocations();
		
		assertTrue(dao.isWebserviceInvocationAllowed(topArtists));
		dao.logWebserviceInvocation(topArtists);
		assertFalse(dao.isWebserviceInvocationAllowed(topArtists));
	}

	@Test
	public void periodIsSignificantForAllowance() {
		Calltype TOP_ARTISTS = Calltype.USER_GET_TOP_ARTISTS;
		LastFmUser user = new LastFmUser("arnathalon");
		WebserviceInvocation topArtists1 = new WebserviceInvocation(TOP_ARTISTS, user, OVERALL.getDays());
		WebserviceInvocation topArtists2 = new WebserviceInvocation(TOP_ARTISTS, user, SIX_MONTHS.getDays());

		deleteWebserviceInvocations();

		assertTrue(dao.isWebserviceInvocationAllowed(topArtists1));
		assertTrue(dao.isWebserviceInvocationAllowed(topArtists2));

		dao.logWebserviceInvocation(topArtists1);
		assertFalse(dao.isWebserviceInvocationAllowed(topArtists1));
		assertTrue(dao.isWebserviceInvocationAllowed(topArtists2));

		dao.logWebserviceInvocation(topArtists2);
		assertFalse(dao.isWebserviceInvocationAllowed(topArtists1));
		assertFalse(dao.isWebserviceInvocationAllowed(topArtists2));
	}
	
	@Test
	public void differentUsersDontInterfere() {
		Calltype TOP_ARTISTS = Calltype.USER_GET_TOP_ARTISTS;
		LastFmUser user1 = new LastFmUser("user1");
		LastFmUser user2 = new LastFmUser("user2");

		WebserviceInvocation topArtists1 = new WebserviceInvocation(TOP_ARTISTS, user1, OVERALL.getDays());
		WebserviceInvocation topArtists2 = new WebserviceInvocation(TOP_ARTISTS, user2, OVERALL.getDays());
		
		deleteWebserviceInvocations();
		
		assertTrue(dao.isWebserviceInvocationAllowed(topArtists1));
		assertTrue(dao.isWebserviceInvocationAllowed(topArtists2));

		dao.logWebserviceInvocation(topArtists2);
		assertTrue(dao.isWebserviceInvocationAllowed(topArtists1));
		assertFalse(dao.isWebserviceInvocationAllowed(topArtists2));
	}

	@Test
	public void importGroupRelatedDataNotPossibleTwice() {
		Calltype GROUP_ARTISTS = Calltype.GROUP_WEEKLY_ARTIST_CHART;
		LastFmGroup group = new LastFmGroup("Brainwashed");
		WebserviceInvocation groupArtists = new WebserviceInvocation(GROUP_ARTISTS, group);
		
		deleteWebserviceInvocations();
		
		assertTrue(dao.isWebserviceInvocationAllowed(groupArtists));
		dao.logWebserviceInvocation(groupArtists);
		assertFalse(dao.isWebserviceInvocationAllowed(groupArtists));
	}

	@Test
	public void differentGroupsDontInterfere() {
		Calltype GROUP_ARTISTS = Calltype.GROUP_WEEKLY_ARTIST_CHART;
		LastFmGroup group1 = new LastFmGroup("Brainwashed");
		LastFmGroup group2 = new LastFmGroup("Dark Ambient");

		WebserviceInvocation groupArtists1 = new WebserviceInvocation(GROUP_ARTISTS, group1);
		WebserviceInvocation groupArtists2 = new WebserviceInvocation(GROUP_ARTISTS, group2);
		
		deleteWebserviceInvocations();
		
		assertTrue(dao.isWebserviceInvocationAllowed(groupArtists1));
		assertTrue(dao.isWebserviceInvocationAllowed(groupArtists2));

		dao.logWebserviceInvocation(groupArtists2);
		
		assertTrue(dao.isWebserviceInvocationAllowed(groupArtists1));
		assertFalse(dao.isWebserviceInvocationAllowed(groupArtists2));
	}

	@Test
	public void importTagRelatedDataNotPossibleTwice() {
		
		tagDao.createTags(Arrays.asList("disco"));
		Tag tag = tagDao.getTags().get(0);
		
		Calltype TOP_ARTISTS = Calltype.TAG_GET_TOP_ARTISTS;
		WebserviceInvocation topArtists = new WebserviceInvocation(TOP_ARTISTS, tag);
		
		deleteWebserviceInvocations();
		
		assertTrue(dao.isWebserviceInvocationAllowed(topArtists));
		dao.logWebserviceInvocation(topArtists);
		assertFalse(dao.isWebserviceInvocationAllowed(topArtists));
	}


	@Test
	public void tagHistoryAllowanceIsBasedOnId() {
		
		tagDao.createTags(Arrays.asList("disco", "sludge"));
		Tag tag1 = tagDao.getTags().get(0);
		Tag tag2 = tagDao.getTags().get(1);
		Calltype TOP_ARTISTS = Calltype.TAG_GET_TOP_ARTISTS;

		WebserviceInvocation topArtists1 = new WebserviceInvocation(TOP_ARTISTS, tag1);
		WebserviceInvocation topArtists2 = new WebserviceInvocation(TOP_ARTISTS, tag2);
		
		deleteWebserviceInvocations();
		
		assertTrue(dao.isWebserviceInvocationAllowed(topArtists1));
		assertTrue(dao.isWebserviceInvocationAllowed(topArtists2));

		dao.logWebserviceInvocation(topArtists2);
		
		assertTrue(dao.isWebserviceInvocationAllowed(topArtists1));
		assertFalse(dao.isWebserviceInvocationAllowed(topArtists2));
		
		dao.logWebserviceInvocation(topArtists2);

		assertTrue(dao.isWebserviceInvocationAllowed(topArtists1));
		assertFalse(dao.isWebserviceInvocationAllowed(topArtists2));

		dao.logWebserviceInvocation(topArtists1);

		assertFalse(dao.isWebserviceInvocationAllowed(topArtists1));
		assertFalse(dao.isWebserviceInvocationAllowed(topArtists2));
	}

	@Test
	public void identifiesArtistsWithoutInvocations() {
		deleteLibraryTracks();
		deleteWebserviceInvocations();

		File file = UnittestLibraryUtil.getFile("Madonna", null, "Jump");
		UnittestLibraryUtil.submitFile(additionDao, file);
		
		final Calltype INFO = ARTIST_GET_INFO, TOP_TRACKS = ARTIST_GET_TOP_TRACKS;
		final String MADONNA = file.getMetadata().getArtist();
		List<String> artistInfo, artistTopTracks;
		artistInfo = dao.getArtistNamesWithNoInvocations(INFO);
		artistTopTracks = dao.getArtistNamesWithNoInvocations(TOP_TRACKS);
		
		Assert.assertNotNull(artistInfo);
		Assert.assertNotNull(artistTopTracks);
		Assert.assertTrue(artistInfo.contains(MADONNA));
		Assert.assertTrue(artistTopTracks.contains(MADONNA));
		
		dao.logWebserviceInvocation(new WebserviceInvocation(INFO, new Artist(MADONNA)));

		artistInfo = dao.getArtistNamesWithNoInvocations(INFO);
		artistTopTracks = dao.getArtistNamesWithNoInvocations(TOP_TRACKS);
		Assert.assertFalse(artistInfo.contains(MADONNA));
		Assert.assertTrue(artistTopTracks.contains(MADONNA));

		dao.logWebserviceInvocation(new WebserviceInvocation(TOP_TRACKS, new Artist(MADONNA)));

		artistInfo = dao.getArtistNamesWithNoInvocations(INFO);
		artistTopTracks = dao.getArtistNamesWithNoInvocations(TOP_TRACKS);
		Assert.assertFalse(artistInfo.contains(MADONNA));
		Assert.assertFalse(artistTopTracks.contains(MADONNA));
	}

	@Test
	public void quarantineLogsInvocationTimeInTheFuture() {
		Calltype TOP = Calltype.ARTIST_GET_TOP_TRACKS;
		Artist artist = new Artist("The Notorious B.I.G feat. G. Dep & Missy Elliott");
		WebserviceInvocation wi = new WebserviceInvocation(TOP, artist);
		assertTrue(dao.isWebserviceInvocationAllowed(wi));
		dao.quarantineWebserviceInvocation(wi);
		assertFalse(dao.isWebserviceInvocationAllowed(wi));

		Date date = dao.getJdbcTemplate().queryForObject(
				"select invocation_time from library.webservice_history hist"
				+ " inner join music.artist a on hist.artist_id = a.id"
				+ " where a.artist_name = upper(?)",
				Date.class, artist.getName());
		Assert.assertTrue(date.after(new Date()));
	}

	@Test
	public void blockLogsInvocationTimeInAnInfiniteFuture() {
		Calltype TOP = Calltype.ARTIST_GET_TOP_TRACKS;
		Artist artist = new Artist("Björn Olsson");
		int artistId = musicDao.getArtistId(artist);

		WebserviceInvocation wi = new WebserviceInvocation(TOP, artist);
		assertTrue(dao.isWebserviceInvocationAllowed(wi));

		dao.blockWebserviceInvocation(artistId, TOP);
		assertFalse(dao.isWebserviceInvocationAllowed(wi));

		dao.getJdbcTemplate().queryForObject(
				"select 1 from library.webservice_history"
				+ " where artist_id = ? and calltype_id = ? and invocation_time = 'infinity'",
				Integer.class, artistId, TOP.getDatabaseId());
	}

	@Test (expected = IllegalArgumentException.class)
	public void artistNullThrowsException() {
		Calltype SIMILAR = Calltype.TRACK_GET_SIMILAR;
		new WebserviceInvocation(SIMILAR, (Artist) null);
	}

	@Test (expected = IllegalArgumentException.class)
	public void trackNullThrowsException() {
		Calltype SIMILAR = Calltype.TRACK_GET_SIMILAR;
		new WebserviceInvocation(SIMILAR, (Track) null);
	}

	private void deleteWebserviceInvocations() {
		dao.getJdbcTemplate().execute("truncate library.webservice_history cascade");
	}

	private void deleteLibraryTracks() {
		dao.getJdbcTemplate().execute("truncate library.track cascade");
	}

}