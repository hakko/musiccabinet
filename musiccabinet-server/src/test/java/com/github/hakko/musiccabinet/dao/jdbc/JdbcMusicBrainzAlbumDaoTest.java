package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_MB_ALBUM;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;
import static junit.framework.Assert.assertEquals;
import static org.apache.commons.lang.StringUtils.reverse;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcMusicBrainzAlbumDaoTest {
	
	@Autowired
	private JdbcMusicDao musicDao;

	@Autowired
	private JdbcMusicBrainzAlbumDao albumDao;
	
	@Autowired
	private JdbcLibraryAdditionDao additionDao;
	
	@Autowired
	private JdbcPlayCountDao playCountDao;
	
	@Autowired
	private JdbcLastFmDao lastFmDao;

	@Autowired
	private JdbcLibraryBrowserDao browserDao;

	private static final String ARTIST = "Cult of Luna",
	TITLE1 = "The Beyond", MBID1 = "236316f7-c919-3986-918b-25e135ba8000", TYPE1 = "Album",
	TITLE2 = "Bodies / Recluse", MBID2 = "5484925b-884c-31d8-9c3e-2ef3824e6a5f", TYPE2 = "EP";
	private static final short YEAR1 = 2003, YEAR2 = 2006;
	private static final String UNKNOWN = "[unknown]";
	private static final String USER = "User";
	
	private Artist artist;
	private MBAlbum album1, album2;
	
	@Before
	public void prepareTestData() throws ApplicationException {
		PostgreSQLUtil.loadFunction(albumDao, UPDATE_MB_ALBUM);

		additionDao.getJdbcTemplate().execute("truncate music.artist cascade");
		additionDao.getJdbcTemplate().execute("truncate library.file cascade");

		artist = new Artist(ARTIST);
		musicDao.setArtistId(artist);
		album1 = new MBAlbum(TITLE1, MBID1, YEAR1, TYPE1);
		album2 = new MBAlbum(TITLE2, MBID2, YEAR2, TYPE2);
		album1.setArtist(artist);
		album2.setArtist(artist);
		albumDao.createAlbums(Arrays.asList(album1, album2));
	}

	@Test
	public void createsAndRetrievesAlbums() {
		List<MBAlbum> albums = albumDao.getAlbums(artist.getId());
		assertEquals(2, albums.size());
		assertEquals(album1.getTitle(), albums.get(0).getTitle());
		assertEquals(album2.getTitle(), albums.get(1).getTitle());
	}
	
	@Test
	public void findsAlbumsMissingFromLibrary() {
		List<MBAlbum> albums = albumDao.getMissingAlbums(null, null, -1, 0);
		
		assertEquals(2, albums.size());
		assertEquals(album1.getTitle(), albums.get(0).getTitle());
		assertEquals(album2.getTitle(), albums.get(1).getTitle());
		
		submitFile(additionDao, getFile(artist.getName(), album1.getTitle(), album1.getTitle()));

		albums = albumDao.getMissingAlbums(null, null, -1, 0);
		assertEquals(1, albums.size());
		assertEquals(album2.getTitle(), albums.get(0).getTitle());

	}

	@Test
	public void findsAlbumsMissingFromLibraryWithNameFilter() {
		// create artist with searchable name in library
		submitFile(additionDao, getFile(artist.getName(), UNKNOWN, UNKNOWN));

		String partialName = ARTIST.substring(ARTIST.lastIndexOf(' ') + 1);
		List<MBAlbum> albums = albumDao.getMissingAlbums(partialName, null, -1, 0);
		assertEquals(2, albums.size());
		
		albums = albumDao.getMissingAlbums(reverse(ARTIST), null, -1, 0);
		assertEquals(0, albums.size());
	}

	@Test
	public void findsAlbumsMissingFromLibraryWithRecentlyPlayedFilter() {
		List<MBAlbum> albums = albumDao.getMissingAlbums(null, USER, 10, 0);
		assertEquals(0, albums.size());
		
		LastFmUser lastFmUser = new LastFmUser(USER);
		lastFmDao.createOrUpdateLastFmUser(lastFmUser);

		submitFile(additionDao, getFile(artist.getName(), UNKNOWN, UNKNOWN));
		Track track = browserDao.getTracks(browserDao.getRandomTrackIds(1)).get(0);
		playCountDao.addPlayCount(lastFmUser, track);
		
		albums = albumDao.getMissingAlbums(null, USER, 10, 0);
		assertEquals(2, albums.size());
	}
	
	@Test
	public void findsAlbumsMissingFromLibraryWithPagination() {
		List<MBAlbum> albums = albumDao.getMissingAlbums(null, null, -1, 1);

		assertEquals(1, albums.size());
	}

}