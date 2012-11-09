package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.LastFmDao;
import com.github.hakko.musiccabinet.dao.LibraryAdditionDao;
import com.github.hakko.musiccabinet.dao.LibraryBrowserDao;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.library.LastFmUser;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcPlayCountDaoTest {

	@Autowired
	private JdbcPlayCountDao dao;
	
	@Autowired
	private LastFmDao lastFmDao;
	
	@Autowired
	private LibraryAdditionDao additionDao;

	@Autowired
	private LibraryBrowserDao browserDao;

	private String username1, username2;
	private LastFmUser user1, user2;
	private Artist artist1, artist2;
	private Album album1, album2;
	private Track track1a, track1b, track2;

	private Comparator<Artist> artistComparator = new Comparator<Artist>() {
		@Override
		public int compare(Artist a1, Artist a2) {
			return a1.getName().compareTo(a2.getName());
		}
	};

	private Comparator<Track> trackComparator = new Comparator<Track>() {
		@Override
		public int compare(Track t1, Track t2) {
			return t1.getName().compareTo(t2.getName());
		}
	};

	@Before
	public void prepareTestData() throws ApplicationException {
		dao.getJdbcTemplate().execute("truncate music.lastfmuser cascade");
		dao.getJdbcTemplate().execute("truncate library.file cascade");
		dao.getJdbcTemplate().execute("truncate music.artist cascade");
		
		user1 = new LastFmUser(username1 = "user1");
		user2 = new LastFmUser(username2 = "user2");
		lastFmDao.createOrUpdateLastFmUser(user1);
		lastFmDao.createOrUpdateLastFmUser(user2);

		File file1 = UnittestLibraryUtil.getFile("artist1", "album1", "title1");
		File file2 = UnittestLibraryUtil.getFile("artist1", "album1", "title2");
		File file3 = UnittestLibraryUtil.getFile("artist2", "album2", "title3");
		submitFile(additionDao, Arrays.asList(file1, file2, file3));
		
		List<Artist> artists = browserDao.getArtists();
		Collections.sort(artists, artistComparator);
		assertEquals(2, artists.size());
		artist1 = artists.get(0);
		artist2 = artists.get(1);
		
		List<Album> albums1 = browserDao.getAlbums(artist1.getId(), true);
		assertEquals(1, albums1.size());
		album1 = albums1.get(0);
		List<Album> albums2 = browserDao.getAlbums(artist2.getId(), true);
		assertEquals(1, albums2.size());
		album2 = albums2.get(0);

		List<Track> tracks1 = browserDao.getTracks(album1.getTrackIds());
		Collections.sort(tracks1, trackComparator);
		assertEquals(2, tracks1.size());
		track1a = tracks1.get(0);
		track1b = tracks1.get(1);
		List<Track> tracks2 = browserDao.getTracks(album2.getTrackIds());
		assertEquals(1, tracks2.size());
		track2 = tracks2.get(0);
	}
	
	@Test
	public void addsPlayCount() {
		dao.addPlayCount(user1, track1a);
		
		List<Integer> artists = dao.getRecentArtists(username1, 0, 10);
		List<Integer> albums = dao.getRecentAlbums(username1, 0, 10);
		List<Integer> tracks = dao.getRecentTracks(username1, 0, 10);
		
		assertNotNull(artists);
		assertNotNull(albums);
		assertNotNull(tracks);
		
		assertEquals(1, artists.size());
		assertEquals(1, albums.size());
		assertEquals(1, tracks.size());
		
		assertEquals(artist1.getId(), artists.get(0).intValue());
		assertEquals(album1.getId(), albums.get(0).intValue());
		assertEquals(track1a.getId(), tracks.get(0).intValue());
	}

	@Test
	public void playCountsAreUserSpecific() {
		dao.addPlayCount(user2, track1a);
		
		assertEquals(1, dao.getRecentArtists(username2, 0, 10).size());
		assertEquals(1, dao.getRecentAlbums(username2, 0, 10).size());
		assertEquals(1, dao.getRecentTracks(username2, 0, 10).size());

		assertEquals(0, dao.getRecentArtists(username1, 0, 10).size());
		assertEquals(0, dao.getRecentAlbums(username1, 0, 10).size());
		assertEquals(0, dao.getRecentTracks(username1, 0, 10).size());
	}
	
	@Test
	public void playCountsAreOrderedByTime() throws InterruptedException {
		dao.addPlayCount(user1, track1b);
		Thread.sleep(1);
		dao.addPlayCount(user1, track1a);
		
		List<Integer> tracks = dao.getRecentTracks(username1, 0, 10);
		assertEquals(track1a.getId(), tracks.get(0).intValue());
		assertEquals(track1b.getId(), tracks.get(1).intValue());
	}
	
	@Test
	public void samePlayCountIsOnlyReturnedOnce() {
		dao.addPlayCount(user2, track1a);
		dao.addPlayCount(user2, track1b);
		dao.addPlayCount(user2, track1b);
		dao.addPlayCount(user2, track1a);
		
		assertEquals(1, dao.getRecentArtists(username2, 0, 10).size());
		assertEquals(1, dao.getRecentAlbums(username2, 0, 10).size());
		assertEquals(2, dao.getRecentTracks(username2, 0, 10).size());
	}

	@Test
	public void resultsArePageable() throws InterruptedException {
		dao.addPlayCount(user1, track1b);
		Thread.sleep(1);
		dao.addPlayCount(user1, track2);
		
		assertEquals(2, dao.getRecentArtists(username1, 0, 2).size());
		assertEquals(2, dao.getRecentAlbums(username1, 0, 2).size());
		assertEquals(2, dao.getRecentTracks(username1, 0, 2).size());

		assertEquals(1, dao.getRecentArtists(username1, 0, 1).size());
		assertEquals(1, dao.getRecentAlbums(username1, 0, 1).size());
		assertEquals(1, dao.getRecentTracks(username1, 0, 1).size());

		assertEquals(1, dao.getRecentArtists(username1, 1, 1).size());
		assertEquals(1, dao.getRecentAlbums(username1, 1, 1).size());
		assertEquals(1, dao.getRecentTracks(username1, 1, 1).size());

		assertEquals(artist2.getId(), dao.getRecentArtists(username1, 0, 1).get(0).intValue());
		assertEquals(album2.getId(), dao.getRecentAlbums(username1, 0, 1).get(0).intValue());
		assertEquals(track2.getId(), dao.getRecentTracks(username1, 0, 1).get(0).intValue());

		assertEquals(artist1.getId(), dao.getRecentArtists(username1, 1, 1).get(0).intValue());
		assertEquals(album1.getId(), dao.getRecentAlbums(username1, 1, 1).get(0).intValue());
		assertEquals(track1b.getId(), dao.getRecentTracks(username1, 1, 1).get(0).intValue());
	}
	
	@Test
	public void returnsMostPlayedArtists() {
		Assert.assertEquals(0, dao.getMostPlayedArtists(username1, 0, 10).size());
		
		dao.addPlayCount(user1, track1a);

		Assert.assertEquals(0, dao.getMostPlayedArtists(username2, 0, 10).size());
		Assert.assertEquals(1, dao.getMostPlayedArtists(username1, 0, 10).size());
		Assert.assertEquals(artist1.getId(), dao.getMostPlayedArtists(username1, 0, 10).get(0).intValue());

		dao.addPlayCount(user1, track2);
		dao.addPlayCount(user1, track2);

		Assert.assertEquals(artist2.getId(), dao.getMostPlayedArtists(username1, 0, 10).get(0).intValue());
		Assert.assertEquals(artist1.getId(), dao.getMostPlayedArtists(username1, 1, 10).get(0).intValue());

		dao.addPlayCount(user1, track1b);
		dao.addPlayCount(user1, track1b);

		Assert.assertEquals(artist1.getId(), dao.getMostPlayedArtists(username1, 0, 10).get(0).intValue());
	}

	@Test
	public void returnsMostPlayedAlbums() {
		Assert.assertEquals(0, dao.getMostPlayedAlbums(username1, 0, 10).size());
		
		dao.addPlayCount(user1, track1a);

		Assert.assertEquals(0, dao.getMostPlayedAlbums(username2, 0, 10).size());
		Assert.assertEquals(1, dao.getMostPlayedAlbums(username1, 0, 10).size());
		Assert.assertEquals(album1.getId(), dao.getMostPlayedAlbums(username1, 0, 10).get(0).intValue());

		dao.addPlayCount(user1, track2);
		dao.addPlayCount(user1, track2);

		Assert.assertEquals(album2.getId(), dao.getMostPlayedAlbums(username1, 0, 10).get(0).intValue());
		Assert.assertEquals(album1.getId(), dao.getMostPlayedAlbums(username1, 1, 10).get(0).intValue());

		dao.addPlayCount(user1, track1b);
		dao.addPlayCount(user1, track1b);

		Assert.assertEquals(album1.getId(), dao.getMostPlayedAlbums(username1, 0, 10).get(0).intValue());
	}

	@Test
	public void returnsMostPlayedTracks() {
		Assert.assertEquals(0, dao.getMostPlayedTracks(username1, 0, 10).size());
		
		dao.addPlayCount(user1, track1a);

		Assert.assertEquals(0, dao.getMostPlayedTracks(username2, 0, 10).size());
		Assert.assertEquals(1, dao.getMostPlayedTracks(username1, 0, 10).size());
	}

}