package com.github.hakko.musiccabinet.service;

import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.ArtistTopTracksDao;
import com.github.hakko.musiccabinet.dao.LibraryAdditionDao;
import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcPlaylistGeneratorDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.PlaylistItem;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class PlaylistGeneratorServiceTest {

	@Autowired
	private PlaylistGeneratorService playlistGeneratorService;

	@Autowired
	private JdbcPlaylistGeneratorDao playlistGeneratorDao;
	
	@Autowired
	private ArtistTopTracksDao artistTopPlaylistItemsDao;

	@Autowired
	private MusicDao musicDao;
	
	@Autowired
	private LibraryAdditionDao additionDao;
	
	private String artistName = "Helios", trackName = "Bless This Morning Year";
	private Artist artist = new Artist(artistName);
	private int artistId;
	
	@Before
	public void prepareTestData() throws ApplicationException {
		PostgreSQLUtil.truncateTables(playlistGeneratorDao);
		
		Track track = new Track(artistName, trackName);
		
		UnittestLibraryUtil.submitFile(additionDao, getFile(track));

		artistId = musicDao.getArtistId(artist);
		
		artistTopPlaylistItemsDao.createTopTracks(artist, Arrays.asList(track));
		
		playlistGeneratorService.updateSearchIndex();
	}
	
	@Test
	public void playlistGeneratorServiceConfigured() {
		Assert.assertNotNull(playlistGeneratorService);
		Assert.assertNotNull(playlistGeneratorService.dao);
	}
	
	@Test
	public void updateSearchIndex() {
		playlistGeneratorService.updateSearchIndex();
	}
	
	@Test
	public void invokeGetPlaylistForArtist() throws ApplicationException {
		List<Integer> playlist = 
			playlistGeneratorService.getPlaylistForArtist(artistId, 3, 20);
		
		Assert.assertNotNull(playlist);
		Assert.assertEquals(1, playlist.size());
	}

	@Test
	public void invokeGetTopPlaylistItemsForArtist() throws ApplicationException {
		List<Integer> playlist = 
			playlistGeneratorService.getTopTracksForArtist(artistId, 25);
		
		Assert.assertNotNull(playlist);
		Assert.assertEquals(1, playlist.size());
	}
	
	@Test
	public void invokeGetTopPlaylistItemsForTags() {
		playlistGeneratorService.getPlaylistForTags(new String[]{"indie", "pop"}, 1, 25);
	}
	
	@Test
	public void noAdjacentArtistsInPlaylist() {
		List<PlaylistItem> ts = new ArrayList<>();
		ts.add(new PlaylistItem(1, 1));
		ts.add(new PlaylistItem(1, 2));
		ts.add(new PlaylistItem(1, 3));
		ts.add(new PlaylistItem(2, 4));
		ts.add(new PlaylistItem(2, 5));
		ts.add(new PlaylistItem(2, 6));
		ts.add(new PlaylistItem(3, 7));
		ts.add(new PlaylistItem(3, 8));
		ts.add(new PlaylistItem(3, 9));
		ts.add(new PlaylistItem(4, 10));
		ts.add(new PlaylistItem(4, 11));
		ts.add(new PlaylistItem(4, 12));
		ts.add(new PlaylistItem(5, 13));
		ts.add(new PlaylistItem(5, 14));
		ts.add(new PlaylistItem(5, 15));
		ts.add(new PlaylistItem(6, 16));
		ts.add(new PlaylistItem(6, 17));
		ts.add(new PlaylistItem(6, 18));
		ts.add(new PlaylistItem(7, 19));
		ts.add(new PlaylistItem(7, 20));
		ts.add(new PlaylistItem(7, 21));
		ts.add(new PlaylistItem(8, 22));
		ts.add(new PlaylistItem(8, 23));
		ts.add(new PlaylistItem(8, 24));

		Random rnd = new Random(1258114665843L);
		for (int i = 0; i < 1000; i++) {
			Collections.shuffle(ts, rnd);
			playlistGeneratorService.distributeArtists(ts);
			
			for (int j = 1; j < ts.size(); j++) {
				if (ts.get(j).getArtistId() == ts.get(j - 1).getArtistId()) {
					Assert.fail("Found adjacent artists in list!");
				}
			}
		}
		
	}
	
}