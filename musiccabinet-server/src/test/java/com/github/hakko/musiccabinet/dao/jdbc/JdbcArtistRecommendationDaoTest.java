package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.util.UnittestLibraryUtil.getFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.ArtistInfoDao;
import com.github.hakko.musiccabinet.dao.ArtistRelationDao;
import com.github.hakko.musiccabinet.dao.ArtistTopTracksDao;
import com.github.hakko.musiccabinet.dao.LibraryAdditionDao;
import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.library.File;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistInfo;
import com.github.hakko.musiccabinet.domain.model.music.ArtistRelation;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.exception.ApplicationException;
import com.github.hakko.musiccabinet.service.PlaylistGeneratorService;
import com.github.hakko.musiccabinet.util.UnittestLibraryUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcArtistRecommendationDaoTest {

	@Autowired
	private JdbcArtistRecommendationDao artistRecommendationDao;

	@Autowired
	private ArtistRelationDao artistRelationDao;
	
	@Autowired
	private ArtistTopTracksDao artistTopTracksDao;
	
	@Autowired
	private MusicDao musicDao;

	@Autowired
	private ArtistInfoDao artistInfoDao;

	@Autowired
	private LibraryAdditionDao additionDao;
	
	@Autowired
	private PlaylistGeneratorService playlistGeneratorService;

	private Artist cher = new Artist("Cher"),
	madonna = new Artist("Madonna"), 
	cyndi = new Artist("Cyndi Lauper"), 
	celine = new Artist("Céline Dion"), 
	kylie = new Artist("Kylie Minogue");

	private int cherId;
	
	@Before
	public void createTestData() throws ApplicationException {
		PostgreSQLUtil.truncateTables(artistRecommendationDao);

		List<ArtistRelation> artistRelations = new ArrayList<>();
		for (Artist targetArtist : Arrays.asList(madonna, cyndi, celine, kylie)) {
			artistRelations.add(new ArtistRelation(targetArtist, 0.33f));
		}
		artistRelationDao.createArtistRelations(cher, artistRelations);

		Track track1, track2, track3;
		artistTopTracksDao.createTopTracks(madonna, Arrays.asList(
				track1 = new Track(madonna, "Like A Prayer"),
				track2 = new Track(madonna, "Hung Up"),
				new Track(madonna, "Frozen")));
		artistTopTracksDao.createTopTracks(cyndi, Arrays.asList(
				track3 = new Track(cyndi, "Time After Time"),
				new Track(cyndi, "Girls Just Wanna Have Fun")));
		artistTopTracksDao.createTopTracks(celine, Arrays.asList(
				new Track(celine, "My Heart Will Go On")));
		artistTopTracksDao.createTopTracks(kylie, Arrays.asList(
				new Track(kylie, "Love At First Sight")));
		
		List<File> files = new ArrayList<>();
		for (Track track : Arrays.asList(track1, track2, track3)) {
			files.add(getFile(track));
		}
		UnittestLibraryUtil.submitFile(additionDao, files);
		
		playlistGeneratorService.updateSearchIndex();

		cherId = musicDao.getArtistId(cher);

		List<ArtistInfo> artistInfos = new ArrayList<>();
		for (Artist artist : Arrays.asList(madonna, cyndi, celine, kylie)) {
			artistInfos.add(new ArtistInfo(artist, "/image/for/" + artist.getName()));
		}
		artistInfoDao.createArtistInfo(artistInfos);
	}
	
	@Test
	public void validateRecommendedArtists() {
		List<String> recommendedArtistNames = 
			artistRecommendationDao.getRecommendedArtistsNotInLibrary(cherId, 10, true);
		
		Assert.assertNotNull(recommendedArtistNames);
		Assert.assertEquals(2, recommendedArtistNames.size());

		List<Artist> artists = new ArrayList<>();
		for (String artistName : recommendedArtistNames) {
			artists.add(new Artist(artistName));
		}

		Assert.assertTrue(artists.contains(celine));
		Assert.assertTrue(artists.contains(kylie));

		Assert.assertFalse(artists.contains(cher));
		Assert.assertFalse(artists.contains(madonna));
		Assert.assertFalse(artists.contains(cyndi));
	}

	@Test
	public void validateArtistsInLibrary() {
		List<ArtistRecommendation> artistRecommendations = 
			artistRecommendationDao.getRecommendedArtistsInLibrary(cherId, 10, true);
		
		Assert.assertNotNull(artistRecommendations);
		Assert.assertEquals(2, artistRecommendations.size());

		List<Artist> artists = Arrays.asList(
				new Artist(artistRecommendations.get(0).getArtistName()), 
				new Artist(artistRecommendations.get(1).getArtistName()));

		Assert.assertTrue(artists.contains(madonna));
		Assert.assertTrue(artists.contains(cyndi));

		Assert.assertFalse(artists.contains(cher));
		Assert.assertFalse(artists.contains(celine));
		Assert.assertFalse(artists.contains(kylie));
	}
	
	@Test
	public void validateMatchingSongCount() {
		int matchingSongs = artistRecommendationDao.getNumberOfRelatedSongs(cherId);

		Assert.assertEquals(3, matchingSongs);
	}

}