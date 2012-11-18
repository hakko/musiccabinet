package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.ADD_TO_LIBRARY;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcStarDaoTest {

	@Autowired
	private JdbcStarDao starDao;

	@Autowired
	private JdbcLibraryAdditionDao additionDao;

	@Autowired
	private JdbcLibraryBrowserDao browserDao;

	@Autowired
	private JdbcLastFmDao lastFmDao;

	private String user1 = "joanofarctan", user2 = "rj";
	private LastFmUser lastFmUser1, lastFmUser2;
	private String artistName1 = "Kylie Minogue", artistName2 = "Madonna";
	private String albumName1 = "Fever", albumName2 = "Like A Virgin";
	private String trackName1 = "More More More", trackName2 = "Material Girl";
	private Artist artist1, artist2;
	private Album album1, album2;
	private Track track1, track2;

	@Before
	public void prepareTestData() throws ApplicationException {
		PostgreSQLUtil.loadFunction(starDao, ADD_TO_LIBRARY);
		
		starDao.getJdbcTemplate().execute("truncate music.artist cascade");
		starDao.getJdbcTemplate().execute("truncate library.file cascade");

		submitFile(additionDao, UnittestLibraryUtil.getFile(artistName1, albumName1, trackName1));
		submitFile(additionDao, UnittestLibraryUtil.getFile(artistName2, albumName2, trackName2));

		List<Artist> artists = browserDao.getArtists();
		Assert.assertEquals(2, artists.size());
		album1 = browserDao.getAlbums((artist1 = artists.get(0)).getId(), true).get(0);
		album2 = browserDao.getAlbums((artist2 = artists.get(1)).getId(), true).get(0);
		
		track1 = browserDao.getTracks(album1.getTrackIds()).get(0);
		track2 = browserDao.getTracks(album2.getTrackIds()).get(0);

		lastFmUser1 = new LastFmUser(user1);
		lastFmDao.createOrUpdateLastFmUser(lastFmUser1);
		lastFmUser2 = new LastFmUser(user2);
		lastFmDao.createOrUpdateLastFmUser(lastFmUser2);
	}

	@Test
	public void nothingIsStarredInitially() {
		Assert.assertEquals(0, starDao.getStarredArtistIds(lastFmUser1, 0, 10, null).size());
		Assert.assertEquals(0, starDao.getStarredArtistIds(lastFmUser1, 0, 10, artistName1).size());

		Assert.assertEquals(0, starDao.getStarredAlbumIds(lastFmUser1, 0, 10, null).size());
		Assert.assertEquals(0, starDao.getStarredAlbumIds(lastFmUser1, 0, 10, albumName1).size());

		Assert.assertEquals(0, starDao.getStarredTrackIds(lastFmUser1, 0, 10, null).size());
		Assert.assertEquals(0, starDao.getStarredTrackIds(lastFmUser1, 0, 10, trackName1).size());
	}
	
	@Test
	public void canStarTrack() {
		starDao.starTrack(lastFmUser1, track1.getId());
		
		Assert.assertEquals(1, starDao.getStarredTrackIds(lastFmUser1, 0, 10, null).size());
		Assert.assertEquals(1, starDao.getStarredTrackIds(lastFmUser1, 0, 10, trackName1).size());
		Assert.assertEquals(0, starDao.getStarredTrackIds(lastFmUser1, 0, 10, trackName2).size());
		Assert.assertEquals(track1.getId(), starDao.getStarredTrackIds(lastFmUser1, 0, 10, null).get(0).intValue());

		Assert.assertEquals(0, starDao.getStarredTrackIds(lastFmUser2, 0, 10, null).size());
		Assert.assertEquals(0, starDao.getStarredTrackIds(lastFmUser2, 0, 10, trackName1).size());

		starDao.starTrack(lastFmUser1, track2.getId());

		Assert.assertEquals(2, starDao.getStarredTrackIds(lastFmUser1, 0, 10, null).size());
		Assert.assertEquals(track2.getId(), starDao.getStarredTrackIds(lastFmUser1, 0, 1, null).get(0).intValue());
		Assert.assertEquals(track1.getId(), starDao.getStarredTrackIds(lastFmUser1, 1, 1, null).get(0).intValue());

		starDao.starTrack(lastFmUser2, track2.getId());
		Assert.assertEquals(1, starDao.getStarredTrackIds(lastFmUser2, 0, 10, null).size());

		starDao.unstarTrack(lastFmUser1, track1.getId());
		starDao.unstarTrack(lastFmUser1, track2.getId());

		Assert.assertEquals(1, starDao.getStarredTrackIds(lastFmUser2, 0, 10, null).size());

		starDao.unstarTrack(lastFmUser2, track2.getId());

		Assert.assertEquals(0, starDao.getStarredTrackIds(lastFmUser1, 0, 10, null).size());
		Assert.assertEquals(0, starDao.getStarredTrackIds(lastFmUser2, 0, 10, null).size());
	}

	@Test
	public void canStarAlbum() {
		starDao.starAlbum(lastFmUser1, album1.getId());
		
		Assert.assertEquals(1, starDao.getStarredAlbumIds(lastFmUser1, 0, 10, null).size());
		Assert.assertEquals(1, starDao.getStarredAlbumIds(lastFmUser1, 0, 10, albumName1).size());
		Assert.assertEquals(0, starDao.getStarredAlbumIds(lastFmUser1, 0, 10, albumName2).size());
		Assert.assertEquals(album1.getId(), starDao.getStarredAlbumIds(lastFmUser1, 0, 10, null).get(0).intValue());

		Assert.assertEquals(0, starDao.getStarredAlbumIds(lastFmUser2, 0, 10, null).size());
		Assert.assertEquals(0, starDao.getStarredAlbumIds(lastFmUser2, 0, 10, albumName1).size());

		starDao.starAlbum(lastFmUser1, album2.getId());

		Assert.assertEquals(2, starDao.getStarredAlbumIds(lastFmUser1, 0, 10, null).size());
		Assert.assertEquals(album2.getId(), starDao.getStarredAlbumIds(lastFmUser1, 0, 1, null).get(0).intValue());
		Assert.assertEquals(album1.getId(), starDao.getStarredAlbumIds(lastFmUser1, 1, 1, null).get(0).intValue());

		starDao.starAlbum(lastFmUser2, album2.getId());
		Assert.assertEquals(1, starDao.getStarredAlbumIds(lastFmUser2, 0, 10, null).size());

		starDao.unstarAlbum(lastFmUser1, album1.getId());
		starDao.unstarAlbum(lastFmUser1, album2.getId());

		Assert.assertEquals(1, starDao.getStarredAlbumIds(lastFmUser2, 0, 10, null).size());

		starDao.unstarAlbum(lastFmUser2, album2.getId());

		Assert.assertEquals(0, starDao.getStarredAlbumIds(lastFmUser1, 0, 10, null).size());
		Assert.assertEquals(0, starDao.getStarredAlbumIds(lastFmUser2, 0, 10, null).size());
		
	}
	
	@Test
	public void canStarArtist() {
		starDao.starArtist(lastFmUser1, artist1.getId());
		
		Assert.assertEquals(1, starDao.getStarredArtistIds(lastFmUser1, 0, 10, null).size());
		Assert.assertEquals(1, starDao.getStarredArtistIds(lastFmUser1, 0, 10, artistName1).size());
		Assert.assertEquals(0, starDao.getStarredArtistIds(lastFmUser1, 0, 10, artistName2).size());
		Assert.assertEquals(artist1.getId(), starDao.getStarredArtistIds(lastFmUser1, 0, 10, null).get(0).intValue());

		Assert.assertEquals(0, starDao.getStarredArtistIds(lastFmUser2, 0, 10, null).size());
		Assert.assertEquals(0, starDao.getStarredArtistIds(lastFmUser2, 0, 10, artistName1).size());

		starDao.starArtist(lastFmUser1, artist2.getId());

		Assert.assertEquals(2, starDao.getStarredArtistIds(lastFmUser1, 0, 10, null).size());
		Assert.assertEquals(artist2.getId(), starDao.getStarredArtistIds(lastFmUser1, 0, 1, null).get(0).intValue());
		Assert.assertEquals(artist1.getId(), starDao.getStarredArtistIds(lastFmUser1, 1, 1, null).get(0).intValue());

		starDao.starArtist(lastFmUser2, artist2.getId());
		Assert.assertEquals(1, starDao.getStarredArtistIds(lastFmUser2, 0, 10, null).size());

		starDao.unstarArtist(lastFmUser1, artist1.getId());
		starDao.unstarArtist(lastFmUser1, artist2.getId());

		Assert.assertEquals(1, starDao.getStarredArtistIds(lastFmUser2, 0, 10, null).size());

		starDao.unstarArtist(lastFmUser2, artist2.getId());

		Assert.assertEquals(0, starDao.getStarredArtistIds(lastFmUser1, 0, 10, null).size());
		Assert.assertEquals(0, starDao.getStarredArtistIds(lastFmUser2, 0, 10, null).size());
		
	}

}