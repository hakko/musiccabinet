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
import com.github.hakko.musiccabinet.dao.TagDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.aggr.ArtistRecommendation;
import com.github.hakko.musiccabinet.domain.model.aggr.TagTopArtists;
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
	private TagDao tagDao;

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

		tagDao.createTopArtists(Arrays.asList(new TagTopArtists("disco",
				Arrays.asList(cher, madonna, cyndi, celine, kylie))));
		
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
	public void validateRelatedArtistsInLibrary() {
		List<ArtistRecommendation> relatedArtists = 
			artistRecommendationDao.getRelatedArtistsInLibrary(cherId, 10, true);
		
		Assert.assertNotNull(relatedArtists);
		Assert.assertEquals(2, relatedArtists.size());

		List<Artist> artists = Arrays.asList(
				new Artist(relatedArtists.get(0).getArtistName()), 
				new Artist(relatedArtists.get(1).getArtistName()));

		Assert.assertTrue(artists.contains(madonna));
		Assert.assertTrue(artists.contains(cyndi));

		Assert.assertFalse(artists.contains(cher));
		Assert.assertFalse(artists.contains(celine));
		Assert.assertFalse(artists.contains(kylie));
	}
	
	@Test
	public void validateRelatedArtistsNotInLibrary() {
		List<String> relatedArtistNames = 
			artistRecommendationDao.getRelatedArtistsNotInLibrary(cherId, 10, true);
		
		Assert.assertNotNull(relatedArtistNames);
		Assert.assertEquals(2, relatedArtistNames.size());

		List<Artist> artists = new ArrayList<>();
		for (String artistName : relatedArtistNames) {
			artists.add(new Artist(artistName));
		}

		Assert.assertTrue(artists.contains(celine));
		Assert.assertTrue(artists.contains(kylie));

		Assert.assertFalse(artists.contains(cher));
		Assert.assertFalse(artists.contains(madonna));
		Assert.assertFalse(artists.contains(cyndi));
	}

	@Test
	public void validateGenreArtistsNotInLibrary() {
		List<String> relatedArtistNames = 
			artistRecommendationDao.getGenreArtistsNotInLibrary("disco", 10, true);
		
		Assert.assertNotNull(relatedArtistNames);
		Assert.assertEquals(3, relatedArtistNames.size());

		List<Artist> artists = new ArrayList<>();
		for (String artistName : relatedArtistNames) {
			artists.add(new Artist(artistName));
		}

		Assert.assertTrue(artists.contains(celine));
		Assert.assertTrue(artists.contains(kylie));
		Assert.assertTrue(artists.contains(cher));

		Assert.assertFalse(artists.contains(madonna));
		Assert.assertFalse(artists.contains(cyndi));
	}

}