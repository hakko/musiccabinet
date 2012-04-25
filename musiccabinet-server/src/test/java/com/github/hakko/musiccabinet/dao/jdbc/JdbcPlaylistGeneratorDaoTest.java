package com.github.hakko.musiccabinet.dao.jdbc;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.ArtistRelationDao;
import com.github.hakko.musiccabinet.dao.ArtistTopTracksDao;
import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.dao.MusicFileDao;
import com.github.hakko.musiccabinet.dao.TrackRelationDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.PlaylistItem;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.domain.model.music.TrackRelation;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistSimilarityParser;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistSimilarityParserImpl;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTracksParser;
import com.github.hakko.musiccabinet.parser.lastfm.ArtistTopTracksParserImpl;
import com.github.hakko.musiccabinet.parser.lastfm.TrackSimilarityParser;
import com.github.hakko.musiccabinet.parser.lastfm.TrackSimilarityParserImpl;
import com.github.hakko.musiccabinet.util.ResourceUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcPlaylistGeneratorDaoTest {

	@Autowired
	private JdbcPlaylistGeneratorDao playlistGeneratorDao;

	@Autowired
	private ArtistRelationDao artistRelationDao;
	
	@Autowired
	private ArtistTopTracksDao artistTopTracksDao;
	
	@Autowired
	private TrackRelationDao trackRelationDao;
	
	@Autowired
	private MusicDao musicDao;
	
	@Autowired
	private MusicFileDao musicFileDao;

	private static final String CHER_SIMILAR_ARTISTS = 
		"last.fm/xml/similarartists.cher.xml";
	private static final String CHER_TOP_TRACKS = 
		"last.fm/xml/toptracks.cher.xml";
	private static final String CHER_SIMILAR_TRACKS = 
		"last.fm/xml/similartracks.cher.believe.xml";

	@Test
	public void testdataOnClasspath() {
		new ResourceUtil(CHER_SIMILAR_ARTISTS);
		new ResourceUtil(CHER_TOP_TRACKS);
		new ResourceUtil(CHER_SIMILAR_TRACKS);
	}

	@Before
	public void clearTopTracksAndRelations() throws ApplicationException {
		PostgreSQLUtil.truncateTables(playlistGeneratorDao);
	}

	@Test
	public void preparingTestdataCreatesSearchIndex() throws ApplicationException {
		prepareTestdataForArtist();
		
		Assert.assertTrue(playlistGeneratorDao.isSearchIndexCreated());
	}
	
	@Test
	public void addCherRelationsAndTopTracksAndGetPlaylist() throws ApplicationException {
		int artistId = prepareTestdataForArtist();
		
		List<PlaylistItem> ts = playlistGeneratorDao.getPlaylistForArtist(artistId);
		
		Assert.assertNotNull(ts);
		Assert.assertEquals(3, ts.size());
	}
	
	@Test
	public void getTopTracks() throws ApplicationException {
		int artistId = prepareTestdataForArtist();
		
		List<String> ts = playlistGeneratorDao.getTopTracksForArtist(artistId);
		
		Assert.assertNotNull(ts);
		Assert.assertEquals(20, ts.size());
	}
	
	
	@Test
	public void addCherSimilarTracksAndGetPlaylist() throws ApplicationException {
		TrackSimilarityParser tsParser = new TrackSimilarityParserImpl(
				new ResourceUtil(CHER_SIMILAR_TRACKS).getInputStream());
		trackRelationDao.createTrackRelations(
				tsParser.getTrack(), tsParser.getTrackRelations());
		
		musicFileDao.clearImport();
		List<MusicFile> musicFiles = new ArrayList<MusicFile>();
		for (TrackRelation tr : tsParser.getTrackRelations()) {
			String artistName = tr.getTarget().getArtist().getName();
			if ("Madonna".equals(artistName)) {
				musicFiles.add(new MusicFile(artistName, tr.getTarget().getName(),
						"/path/to/" + tr.getTarget().getName(), 0L, 0L));
			}
		}
		musicFileDao.addMusicFiles(musicFiles);
		musicFileDao.createMusicFiles();
		
		int trackId = musicDao.getTrackId(tsParser.getTrack().getArtist().getName(), 
				tsParser.getTrack().getName());
		
		playlistGeneratorDao.updateSearchIndex();
		List<PlaylistItem> ts = playlistGeneratorDao.getPlaylistForTrack(trackId);
		
		Assert.assertNotNull(ts);
		Assert.assertEquals(2, ts.size());
	}

	private int prepareTestdataForArtist() throws ApplicationException {
		ArtistSimilarityParser asParser = new ArtistSimilarityParserImpl(
				new ResourceUtil(CHER_SIMILAR_ARTISTS).getInputStream());
		artistRelationDao.createArtistRelations(
				asParser.getArtist(), asParser.getArtistRelations());

		ArtistTopTracksParser attParser = new ArtistTopTracksParserImpl(
				new ResourceUtil(CHER_TOP_TRACKS).getInputStream());
		artistTopTracksDao.createTopTracks(
				attParser.getArtist(), attParser.getTopTracks());
		
		musicFileDao.clearImport();
		List<MusicFile> musicFiles = new ArrayList<MusicFile>();
		for (Track topTrack : attParser.getTopTracks()) {
			MusicFile mf = new MusicFile(topTrack.getArtist().getName(), 
					topTrack.getName(), "/path/to/" + topTrack.getName(), 0L, 0L);
			musicFiles.add(mf);
		}
		musicFileDao.addMusicFiles(musicFiles);
		musicFileDao.createMusicFiles();

		int artistId = musicDao.getArtistId(asParser.getArtist().getName());
		
		playlistGeneratorDao.updateSearchIndex();

		return artistId;
	}

}