package com.github.hakko.musiccabinet.service;

import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;
import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.submitFile;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcLastFmDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryAdditionDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryBrowserDao;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class StarServiceTest {

	@Autowired
	private StarService starService;
	
	@Autowired
	private JdbcLibraryAdditionDao additionDao;

	@Autowired
	private JdbcLibraryBrowserDao browserDao;
	
	@Autowired
	private JdbcLastFmDao lastFmDao;
	
	private Artist artist;
	private Album album;
	private Track track;
	
	private String user1 = "user1", user2 = "user2";
	
	@Before
	public void createTestData() {
		additionDao.getJdbcTemplate().execute("truncate library.file cascade");
		additionDao.getJdbcTemplate().execute("truncate music.artist cascade");
		
		submitFile(additionDao, getFile("artist", "album", "title"));
		
		artist = browserDao.getArtists().get(0);
		album = browserDao.getAlbums(artist.getId(), true).get(0);
		track = browserDao.getTracks(album.getTrackIds()).get(0);
		
		lastFmDao.getLastFmUserId(user1);
		lastFmDao.getLastFmUserId(user2);
	}
	
	@Test
	public void serviceCachesStarredArtistsAndUsers() {
		Assert.assertFalse(starService.isArtistStarred(user1, artist.getId()));
		
		starService.starArtist(user1, artist.getId());
		
		Assert.assertTrue(starService.isArtistStarred(user1, artist.getId()));
		Assert.assertFalse(starService.isArtistStarred(user2, artist.getId()));
		Assert.assertFalse(starService.isArtistStarred(user1, artist.getId() + 1));
	}

	@Test
	public void serviceCachesStarredAlbumsAndUsers() {
		boolean[] mask = starService.getStarredAlbumsMask(user1, Arrays.asList(album.getId()));
		Assert.assertEquals(1, mask.length);
		Assert.assertEquals(false, mask[0]);
		
		starService.starAlbum(user1, album.getId());
		mask = starService.getStarredAlbumsMask(user1, Arrays.asList(album.getId()));
		Assert.assertEquals(true, mask[0]);

		mask = starService.getStarredAlbumsMask(user1, Arrays.asList(album.getId()+1));
		Assert.assertEquals(false, mask[0]);

		mask = starService.getStarredAlbumsMask(user2, Arrays.asList(album.getId()));
		Assert.assertEquals(false, mask[0]);
		
		starService.unstarAlbum(user1, album.getId());

		mask = starService.getStarredAlbumsMask(user1, Arrays.asList(album.getId()));
		Assert.assertEquals(false, mask[0]);
	}

	@Test
	public void serviceCachesStarredTracksAndUsers() {
		boolean[] mask = starService.getStarredTracksMask(user1, Arrays.asList(track.getId()));
		Assert.assertEquals(1, mask.length);
		Assert.assertEquals(false, mask[0]);
		
		starService.starTrack(user1, track.getId());
		mask = starService.getStarredTracksMask(user1, Arrays.asList(track.getId()));
		Assert.assertEquals(true, mask[0]);

		mask = starService.getStarredTracksMask(user1, Arrays.asList(track.getId()+1));
		Assert.assertEquals(false, mask[0]);

		mask = starService.getStarredTracksMask(user2, Arrays.asList(track.getId()));
		Assert.assertEquals(false, mask[0]);
		
		starService.unstarTrack(user1, track.getId());

		mask = starService.getStarredTracksMask(user1, Arrays.asList(track.getId()));
		Assert.assertEquals(false, mask[0]);
	}

	@Test
	public void returnsStarredArtists() {
		List<Artist> artists = starService.getStarredArtists(user1);
		Assert.assertNotNull(artists);
		Assert.assertEquals(0, artists.size());
		
		starService.starArtist(user1, artist.getId());

		artists = starService.getStarredArtists(user1);
		Assert.assertNotNull(artists);
		Assert.assertEquals(1, artists.size());
		Assert.assertEquals(artist, artists.get(0));
		
		starService.unstarArtist(user1, artist.getId());

		artists = starService.getStarredArtists(user1);
		Assert.assertNotNull(artists);
		Assert.assertEquals(0, artists.size());
	}
	
}