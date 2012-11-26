package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_MB_ARTIST;
import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.MB_ARTIST_QUERY;
import static com.github.hakko.musiccabinet.domain.model.library.WebserviceInvocation.Calltype.MB_RELEASE_GROUPS;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;
import static junit.framework.Assert.assertEquals;
import static org.apache.commons.lang.StringUtils.reverse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.MBArtist;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcMusicBrainzArtistDaoTest {
	
	@Autowired
	private JdbcLibraryBrowserDao browserDao;

	@Autowired
	private JdbcLibraryAdditionDao additionDao;
	
	@Autowired
	private JdbcMusicBrainzArtistDao artistDao;
	
	private String artistName1 = "Madonna", artistName2 = "Beatles";
	private String albumName = "Album name", trackName = "Track name";
	private Artist artist1, artist2;
	
	private static final String MBID = "d347406f-839d-4423-9a28-188939282afa",
			COUNTRY_CODE = "SE";
	private static final short START_YEAR = 1998;
	private static final boolean ACTIVE = true;
	
	@Before
	public void prepareTestData() throws ApplicationException {
		PostgreSQLUtil.loadFunction(artistDao, UPDATE_MB_ARTIST);
		
		browserDao.getJdbcTemplate().execute("truncate music.artist cascade");
		browserDao.getJdbcTemplate().execute("truncate library.file cascade");

		submitFile(additionDao, getFile(artistName1, albumName, trackName));
		submitFile(additionDao, getFile(artistName2, albumName, trackName));

		List<Artist> artists = browserDao.getArtists();
		assertEquals(2, artists.size());
		artist1 = artists.get(0);
		artist2 = artists.get(1);
	}

	@Test
	public void createsArtists() {
		artistDao.createArtists(Arrays.asList(new MBArtist(artist1.getName(), 
				MBID, COUNTRY_CODE, START_YEAR, ACTIVE)));
		
		MBArtist artist = artistDao.getArtist(artist1.getId());
		
		assertEquals(artist1.getName(), artist.getName());
		assertEquals(MBID, artist.getMbid());
		assertEquals(COUNTRY_CODE, artist.getCountryCode());
		assertEquals(START_YEAR, artist.getStartYear());
		assertEquals(ACTIVE, artist.isActive());
	}
	
	@Test
	public void findsMissingArtists() {
		List<Artist> missingArtists = artistDao.getMissingArtists();
		assertEquals(2, missingArtists.size());
		assertEquals(artist1, missingArtists.get(0));
		assertEquals(artist2, missingArtists.get(1));
		
		artistDao.createArtists(Arrays.asList(new MBArtist(artist1.getName(), 
				MBID, COUNTRY_CODE, START_YEAR, ACTIVE)));
		
		missingArtists = artistDao.getMissingArtists();
		assertEquals(1, missingArtists.size());
		assertEquals(artist2, missingArtists.get(0));
	}

	@Test
	public void ignoresMissingArtistsWithoutMusicBrainzIds() {
		List<Artist> missingArtists = artistDao.getMissingArtists();
		assertEquals(2, missingArtists.size());

		artistDao.getJdbcTemplate().execute(String.format(
				"insert into library.webservice_history (artist_id, invocation_time, calltype_id)"
				+ " values (%d, to_timestamp(0), %d)",
				artist1.getId(), MB_ARTIST_QUERY.getDatabaseId()));

		missingArtists = artistDao.getMissingArtists();
		assertEquals(1, missingArtists.size());
		assertEquals(artist2, missingArtists.get(0));

		artistDao.getJdbcTemplate().execute(String.format(
				"insert into library.webservice_history (artist_id, invocation_time, calltype_id)"
				+ " values (%d, now(), %d)",
				artist2.getId(), MB_ARTIST_QUERY.getDatabaseId()));
		
		missingArtists = artistDao.getMissingArtists();
		assertTrue(missingArtists.isEmpty());
	}
	
	@Test
	public void ignoresVariousArtists() {
		List<Artist> missingArtists = artistDao.getMissingArtists();
		assertEquals(2, missingArtists.size());

		submitFile(additionDao, getFile(reverse(artistName2), albumName, trackName));

		missingArtists = artistDao.getMissingArtists();
		assertEquals(3, missingArtists.size());
		
		submitFile(additionDao, getFile("Various Artists", albumName, trackName));
		assertEquals(3, missingArtists.size());
	}
	
	@Test
	public void findsOutdatedArtists() {
		assertEquals(0, artistDao.getOutdatedArtists().size());

		artistDao.createArtists(Arrays.asList(
				new MBArtist(artist1.getName(), MBID, COUNTRY_CODE, START_YEAR, ACTIVE)));

		artistDao.getJdbcTemplate().execute(String.format(
				"insert into library.webservice_history (artist_id, invocation_time, calltype_id)"
				+ " values (%d, to_timestamp(0), %d)",
				artist1.getId(), MB_RELEASE_GROUPS.getDatabaseId()));

		List<MBArtist> outdatedArtists = artistDao.getOutdatedArtists();
		assertEquals(1, outdatedArtists.size());
		assertEquals(artist1.getName(), outdatedArtists.get(0).getName());
	}
	
	@Test
	public void calculatesMissingAndOutdatedArtists() {
		assertEquals(2, artistDao.getMissingAndOutdatedArtistsCount()); // 2 missing artists

		artistDao.createArtists(Arrays.asList(
				new MBArtist(artist1.getName(), MBID, COUNTRY_CODE, START_YEAR, ACTIVE),
				new MBArtist(artist2.getName(), MBID, COUNTRY_CODE, START_YEAR, ACTIVE)));
		
		assertEquals(2, artistDao.getMissingAndOutdatedArtistsCount()); // 2 missing albums
		
		artistDao.getJdbcTemplate().execute(String.format(
				"insert into library.webservice_history (artist_id, invocation_time, calltype_id)"
				+ " values (%d, to_timestamp(0), %d)",
				artist2.getId(), MB_RELEASE_GROUPS.getDatabaseId()));

		assertEquals(2, artistDao.getMissingAndOutdatedArtistsCount()); // 1 missing album, 1 outdated
		
		artistDao.getJdbcTemplate().execute(
				"update library.webservice_history set invocation_time = now()");
		
		assertEquals(1, artistDao.getMissingAndOutdatedArtistsCount()); // 1 missing album
	}
	
	@Test
	public void ignoresVariousArtistsInCalculation() {
		assertEquals(2, artistDao.getMissingAndOutdatedArtistsCount());

		submitFile(additionDao, getFile("Various Artists", albumName, trackName));  // ignored

		assertEquals(2, artistDao.getMissingAndOutdatedArtistsCount());

		submitFile(additionDao, getFile(reverse(artistName2), albumName, trackName));

		assertEquals(3, artistDao.getMissingAndOutdatedArtistsCount());
	}

}