package com.github.hakko.musiccabinet.service;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;

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
import com.github.hakko.musiccabinet.dao.MusicDirectoryDao;
import com.github.hakko.musiccabinet.dao.MusicFileDao;
import com.github.hakko.musiccabinet.dao.TrackRelationDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcPlaylistGeneratorDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.PlaylistItem;
import com.github.hakko.musiccabinet.domain.model.library.MusicDirectory;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.domain.model.music.TrackRelation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class PlaylistGeneratorServiceTest {

	@Autowired
	private PlaylistGeneratorService playlistGeneratorService;

	@Autowired
	private JdbcPlaylistGeneratorDao playlistGeneratorDao;
	
	@Autowired
	private ArtistTopTracksDao artistTopTracksDao;
	
	@Autowired
	private TrackRelationDao trackRelationDao;
	
	@Autowired
	private MusicFileDao musicFileDao;

	@Autowired
	private MusicDirectoryDao musicDirectoryDao;

	private String artistName = "Helios", trackName = "Bless This Morning Year";
	private Artist artist = new Artist(artistName);
	private Track track = new Track(artist, trackName);
	private MusicDirectory musicDirectory = new MusicDirectory(
			artistName, "/path/to/" + artistName);
	private MusicFile musicFile = new MusicFile(artistName, trackName,
			"/path/to/" + artistName + "/" + trackName, 0L, 0L);
	
	@Before
	public void prepareTestData() throws ApplicationException {
		PostgreSQLUtil.truncateTables(playlistGeneratorDao);

		artistTopTracksDao.createTopTracks(artist, Arrays.asList(track));

		trackRelationDao.createTrackRelations(track, asList(new TrackRelation(track, 1f)));
		
		musicFileDao.clearImport();
		musicFileDao.addMusicFiles(Arrays.asList(musicFile));
		musicFileDao.createMusicFiles();

		musicDirectoryDao.clearImport();
		musicDirectoryDao.addMusicDirectories(Arrays.asList(musicDirectory));
		musicDirectoryDao.createMusicDirectories();
		
		playlistGeneratorService.updateSearchIndex();

		// we don't want actual look-up of track relations from last.fm in tests
		TrackRelationService trService = mock(TrackRelationService.class);
		playlistGeneratorService.setTrackRelationService(trService);
	}
	
	@Test
	public void playlistGeneratorServiceConfigured() {
		Assert.assertNotNull(playlistGeneratorService);
		Assert.assertNotNull(playlistGeneratorService.musicFileDao);
		Assert.assertNotNull(playlistGeneratorService.playlistGeneratorDao);
		Assert.assertNotNull(playlistGeneratorService.musicDirectoryDao);
		Assert.assertNotNull(playlistGeneratorService.trackRelationService);
	}
	
	@Test
	public void updateSearchIndex() {
		playlistGeneratorService.updateSearchIndex();
	}
	
	@Test
	public void invokeGetPlaylistForArtist() throws ApplicationException {
		List<String> playlist = 
			playlistGeneratorService.getPlaylistForArtist(musicDirectory.getPath(), 3, 20);
		
		Assert.assertNotNull(playlist);
		Assert.assertEquals(1, playlist.size());
		Assert.assertEquals(musicFile.getPath(), playlist.get(0));
	}

	@Test
	public void invokeGetPlaylistForTrack() throws ApplicationException {
		List<String> playlist = 
			playlistGeneratorService.getPlaylistForTrack(musicFile.getPath());
		
		Assert.assertNotNull(playlist);
		Assert.assertEquals(1, playlist.size());
		Assert.assertEquals(musicFile.getPath(), playlist.get(0));
	}

	@Test
	public void invokeGetTopTracksForArtist() throws ApplicationException {
		List<String> playlist = 
			playlistGeneratorService.getTopTracksForArtist(musicDirectory.getPath(), 25);
		
		Assert.assertNotNull(playlist);
		Assert.assertEquals(1, playlist.size());
		Assert.assertEquals(musicFile.getPath(), playlist.get(0));
	}
	
	@Test
	public void invokeGetTopTracksForTags() {
		playlistGeneratorService.getTopTracksForTags(new String[]{"indie", "pop"}, 1, 25);
	}
	
	@Test
	public void noAdjacentArtistsInPlaylist() {
		List<PlaylistItem> ts = new ArrayList<PlaylistItem>();
		ts.add(new PlaylistItem("A", "P1"));
		ts.add(new PlaylistItem("A", "P2"));
		ts.add(new PlaylistItem("A", "P3"));
		ts.add(new PlaylistItem("B", "P4"));
		ts.add(new PlaylistItem("B", "P5"));
		ts.add(new PlaylistItem("B", "P6"));
		ts.add(new PlaylistItem("C", "P7"));
		ts.add(new PlaylistItem("C", "P8"));
		ts.add(new PlaylistItem("C", "P9"));
		ts.add(new PlaylistItem("D", "P10"));
		ts.add(new PlaylistItem("D", "P11"));
		ts.add(new PlaylistItem("D", "P12"));
		ts.add(new PlaylistItem("E", "P13"));
		ts.add(new PlaylistItem("E", "P14"));
		ts.add(new PlaylistItem("E", "P15"));
		ts.add(new PlaylistItem("F", "P16"));
		ts.add(new PlaylistItem("F", "P17"));
		ts.add(new PlaylistItem("F", "P18"));
		ts.add(new PlaylistItem("G", "P19"));
		ts.add(new PlaylistItem("G", "P20"));
		ts.add(new PlaylistItem("G", "P21"));
		ts.add(new PlaylistItem("H", "P22"));
		ts.add(new PlaylistItem("H", "P23"));
		ts.add(new PlaylistItem("H", "P24"));

		Random rnd = new Random(1258114665843L);
		for (int i = 0; i < 1000; i++) {
			Collections.shuffle(ts, rnd);
			playlistGeneratorService.distributeArtists(ts);
			
			for (int j = 1; j < ts.size(); j++) {
				if (ts.get(j).getArtist().equals(
					ts.get(j - 1).getArtist())) {
					Assert.fail("Found adjacent artists in list!");
				}
			}
		}
		
	}
	
}