package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_ARTISTRELATION_FROM_IMPORT;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.MusicDao;
import com.github.hakko.musiccabinet.dao.MusicFileDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.library.MusicFile;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.ArtistRelation;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcArtistRelationDaoTest {

	/* Test data */
	private Artist sourceArtist1 = new Artist("Cher");
	private ArtistRelation ar1 = new ArtistRelation(new Artist("Céline Dion"), 0.406959f);
	private ArtistRelation ar2 = new ArtistRelation(new Artist("Kylie Minogue"), 0.364589f);
	private ArtistRelation ar3 = new ArtistRelation(new Artist("Tina Turner"), 0.347697f);
	private ArtistRelation ar4 = new ArtistRelation(new Artist("Barbra Streisand"), 0.313871f);
	
	private Artist sourceArtist2 = new Artist("Kylie Minogue");
	private ArtistRelation ar5 = new ArtistRelation(new Artist("Geri Halliwell"), 0.291044f);
	private ArtistRelation ar6 = new ArtistRelation(new Artist("Jennifer Lopez"), 0.208765f);
	private ArtistRelation ar7 = new ArtistRelation(new Artist("Cher"), 0.195111f);

	@Autowired
	private MusicDao musicDao;
	
	@Autowired
	private MusicFileDao musicFileDao;
	
	@Autowired
	private JdbcArtistRelationDao dao;

	@Before
	public void loadFunctionDependency() throws ApplicationException {
		PostgreSQLUtil.loadFunction(dao, UPDATE_ARTISTRELATION_FROM_IMPORT);
		
		PostgreSQLUtil.truncateTables(dao);
	}
	
	@Test
	public void createAndValidateArtistRelations() {
		deleteArtistRelations();

		dao.createArtistRelations(sourceArtist1, Arrays.asList(ar1, ar2, ar3, ar4));
		dao.createArtistRelations(sourceArtist2, Arrays.asList(ar5, ar6, ar7));

		List<ArtistRelation> cherRelations = dao.getArtistRelations(sourceArtist1);
		List<ArtistRelation> kylieRelations = dao.getArtistRelations(sourceArtist2);

		assertNotNull(cherRelations);
		assertNotNull(kylieRelations);
		Assert.assertEquals(4, cherRelations.size());
		Assert.assertEquals(3, kylieRelations.size());
		
		for (ArtistRelation ar : Arrays.asList(ar1, ar2, ar3, ar4)) {
			assertTrue(cherRelations.contains(ar));
		}
		for (ArtistRelation ar : Arrays.asList(ar5, ar6, ar7)) {
			assertTrue(kylieRelations.contains(ar));
		}
		
	}

	@Test
	public void createAndValidateUpdatedArtistRelation() {
		deleteArtistRelations();

		dao.createArtistRelations(sourceArtist1, Arrays.asList(ar1, ar2, ar3, ar4));
		dao.createArtistRelations(sourceArtist2, Arrays.asList(ar5, ar6, ar7));

		ar3.setMatch(0.55f);
		ar7.setMatch(0.44f);

		dao.createArtistRelations(sourceArtist2, Arrays.asList(ar5, ar7));

		List<ArtistRelation> cherRelations = dao.getArtistRelations(sourceArtist1);
		List<ArtistRelation> kylieRelations = dao.getArtistRelations(sourceArtist2);

		assertNotNull(cherRelations);
		assertNotNull(kylieRelations);
		Assert.assertEquals(4, cherRelations.size());
		Assert.assertEquals(3, kylieRelations.size());
		
		for (ArtistRelation ar : Arrays.asList(ar1, ar2, ar4)) {
			assertTrue(cherRelations.contains(ar));
		}
		assertFalse(cherRelations.contains(ar3));
		for (ArtistRelation ar : Arrays.asList(ar5, ar6, ar7)) {
			assertTrue(kylieRelations.contains(ar));
		}
	}
	
	@Test
	public void noArtistsMeanNoMissingRelations() {
		deleteArtists();
		
		List<Artist> artists = dao.getArtistsWithoutRelations();
		Assert.assertNotNull(artists);
		Assert.assertEquals(0, artists.size());
	}
	
	@Test
	public void oneArtistMeansOneMissingRelation() {
		final String artistName = "Piano Magic";
		final String trackName = "Kingfisher / Grass";
		final String path = "/";
		final long lastModified = System.currentTimeMillis(), created = lastModified;
		
		deleteArtists();
		
		createMusicFiles(Arrays.asList(
				new MusicFile(artistName, trackName, path, created, lastModified)));
		
		List<Artist> artists = dao.getArtistsWithoutRelations();
		Assert.assertNotNull(artists);
		Assert.assertEquals(1, artists.size());
		Assert.assertTrue(artistName.equals(artists.get(0).getName()));
		Assert.assertNotNull(artists.get(0).getName());
	}

	@Test
	public void onlyArtistsWithoutRelationsAreReturned() {
		long time = System.currentTimeMillis();
		MusicFile mf1 = new MusicFile("Emily Barker", "Blackbird", "/path1", time, time);
		MusicFile mf2 = new MusicFile("Emily Haines", "Our Hell", "/path2", time, time);
		MusicFile mf3 = new MusicFile("Emily Jane White", "Dagger", "/path3", time, time);
		
		Artist artist1 = mf1.getTrack().getArtist();
		Artist artist2 = mf2.getTrack().getArtist();
		Artist artist3 = mf3.getTrack().getArtist();
		
		createMusicFiles(asList(mf1, mf2, mf3));
		dao.createArtistRelations(artist1, asList(new ArtistRelation(artist3, 0.25f)));
		
		List<Artist> artists = dao.getArtistsWithoutRelations();

		Assert.assertNotNull(artists);
		Assert.assertFalse(artists.contains(artist1));
		Assert.assertTrue(artists.contains(artist2));
		Assert.assertTrue(artists.contains(artist3));
	}
	
	@Test
	public void artistWithoutMusicFilesAreNotReturned() {
		long time = System.currentTimeMillis();
		MusicFile mf = new MusicFile("Jay Munly", "My Darling Sambo", "/path/" + time, time, time);

		createMusicFiles(Arrays.asList(mf));
		musicDao.getArtistId("Jay Farrar");
		
		List<Artist> artists = dao.getArtistsWithoutRelations();
		Assert.assertNotNull(artists);
		Assert.assertEquals(1, artists.size());
		Assert.assertTrue(mf.getTrack().getArtist().getName()
				.equals(artists.get(0).getName()));
	}
	
	private void createMusicFiles(List<MusicFile> musicFiles) {
		musicFileDao.clearImport();
		musicFileDao.addMusicFiles(musicFiles);
		musicFileDao.createMusicFiles();
	}
	
	private void deleteArtists() {
		dao.getJdbcTemplate().execute("truncate music.artist cascade");
	}
	
	private void deleteArtistRelations() {
		dao.getJdbcTemplate().execute("truncate music.artistrelation cascade");
	}
	
}