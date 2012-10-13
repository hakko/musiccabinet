package com.github.hakko.musiccabinet.service;

import static com.github.hakko.musiccabinet.service.library.AudioTagService.UNKNOWN_ALBUM;
import static com.github.hakko.musiccabinet.service.library.LibraryUtil.set;
import static java.io.File.separatorChar;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.jdbc.JdbcLibraryPresenceDao;
import com.github.hakko.musiccabinet.dao.jdbc.JdbcMusicDao;
import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.music.Album;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.Track;
import com.github.hakko.musiccabinet.service.LibraryBrowserService;
import com.github.hakko.musiccabinet.service.library.LibraryScannerService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class LibraryBrowserServiceTest {

	@Autowired
	private LibraryScannerService scannerService;
	
	@Autowired
	private LibraryBrowserService browserService; 

	@Autowired
	private JdbcLibraryPresenceDao presenceDao;

	@Autowired
	private JdbcMusicDao musicDao;
	
	// paths to resources folders containing actual, tagged, mp3 files
	private String library, media1, media2, media3, media4, media5, media6, aretha;
	
	@Before
	public void clearDirectories() throws Exception {
		PostgreSQLUtil.loadAllFunctions(presenceDao);
		presenceDao.getJdbcTemplate().execute("truncate music.artist cascade");
		presenceDao.getJdbcTemplate().execute("truncate library.directory cascade");

		library = new File(currentThread().getContextClassLoader()
				.getResource("library").toURI()).getAbsolutePath();
		media1 = library + separatorChar + "media1";
		media2 = library + separatorChar + "media2";
		media3 = library + separatorChar + "media3";
		media4 = library + separatorChar + "media4";
		media5 = library + separatorChar + "media5";
		media6 = library + separatorChar + "media6";
		aretha = media2 + separatorChar + "Aretha Franklin";
	}
	
	@Test
	public void findsArtists() throws Exception {
		scannerService.add(set(media1));
		assertArtists(browserService.getArtists(), "The Beatles", "Elvis Presley");

		scannerService.add(set(media1, media2));
		assertArtists(browserService.getArtists(), "The Beatles", "Elvis Presley", "Aretha Franklin");

		scannerService.delete(set(media1));
		assertArtists(browserService.getArtists(), "The Beatles", "Aretha Franklin");

		scannerService.delete(set(media2));
		assertArtists(browserService.getArtists());

		scannerService.add(set(aretha));
		assertArtists(browserService.getArtists(), "Aretha Franklin");

		scannerService.add(set(media2));
		assertArtists(browserService.getArtists(), "The Beatles", "Aretha Franklin");
	}
	
	@Test
	public void findsAlbums() throws Exception {
		Artist theBeatles = new Artist("The Beatles");
		int beatlesId = musicDao.getArtistId(theBeatles);
		
		scannerService.add(set(media1));
		assertAlbums(browserService.getAlbums(beatlesId, true), theBeatles, "1962-1966", UNKNOWN_ALBUM);

		scannerService.add(set(media1)); // shouldn't change anything
		assertAlbums(browserService.getAlbums(beatlesId, true), theBeatles, "1962-1966", UNKNOWN_ALBUM);

		scannerService.add(set(media2));
		assertAlbums(browserService.getAlbums(beatlesId, true), theBeatles, "1962-1966", "1967-1970", UNKNOWN_ALBUM);

		scannerService.delete(set(media1));
		assertAlbums(browserService.getAlbums(beatlesId, true), theBeatles, "1967-1970");
		
		scannerService.delete(set(media1)); // shouldn't change anything
		assertAlbums(browserService.getAlbums(beatlesId, true), theBeatles, "1967-1970");

		scannerService.delete(set(media2));
		assertAlbums(browserService.getAlbums(beatlesId, true), theBeatles);
	}

	@Test
	public void findsAlbum() throws Exception {
		Artist theBeatles = new Artist("The Beatles");
		int beatlesId = musicDao.getArtistId(theBeatles);

		scannerService.add(set(media2));

		List<Album> albums = browserService.getAlbums(beatlesId, true);
		assertAlbums(albums, theBeatles, "1967-1970");
		
		int albumId = albums.get(0).getId();
		Album album = browserService.getAlbum(albumId);

		assertAlbums(Arrays.asList(album), theBeatles, "1967-1970");
	}

	@Test
	public void findsArtwork() throws Exception {
		Artist artist = new Artist("Artist Name");
		int artistId = musicDao.getArtistId(artist);
		
		scannerService.add(set(media3));
		List<Album> albums = browserService.getAlbums(artistId, true);
		
		assertAlbums(albums, artist, "Embedded artwork", "Folder artwork");

		Album folderArtAlbum = getAlbum(albums, "Folder artwork");
		Album embeddedArtAlbum = getAlbum(albums, "Embedded artwork");
		
		Assert.assertTrue(embeddedArtAlbum.getCoverArtPath() != null);
		Assert.assertTrue(embeddedArtAlbum.isCoverArtEmbedded());
		Assert.assertTrue(embeddedArtAlbum.getCoverArtURL() == null);

		Assert.assertTrue(folderArtAlbum.getCoverArtPath() != null);
		Assert.assertFalse(folderArtAlbum.isCoverArtEmbedded());
		Assert.assertTrue(folderArtAlbum.getCoverArtURL() == null);
		
		Assert.assertTrue(embeddedArtAlbum.getCoverArtPath().endsWith(
				"Embedded artwork.mp3"));
		Assert.assertTrue(folderArtAlbum.getCoverArtPath().endsWith(
				"folder.png"));
	}
	
	@Test
	public void findsTrack() throws Exception {
		scannerService.add(set(aretha));
		Artist artist = new Artist("Aretha Franklin");
		int artistId = musicDao.getArtistId(artist);
		
		List<Album> albums = browserService.getAlbums(artistId, true);
		assertAlbums(albums, artist, UNKNOWN_ALBUM);
		
		List<Track> tracks = browserService.getTracks(albums.get(0).getTrackIds());
		assertTracks(tracks, Arrays.asList(new Track("Aretha Franklin", "Bridge Over Troubled Water")));
	}

	@Test
	public void findsTracks() throws Exception {
		scannerService.add(set(media1));
		
		Artist artist = new Artist("The Beatles");
		int artistId = musicDao.getArtistId(artist);
		
		List<Album> albums = browserService.getAlbums(artistId, true);
		Album redAlbum = getAlbum(albums, "1962-1966");
		
		List<Track> tracks = browserService.getTracks(redAlbum.getTrackIds());
		List<Track> expectedTracks = new ArrayList<>();
		for (String title : asList("Love Me Do", "Please Please Me", "From Me To You", 
				"She Loves You", "Help!")) {
			expectedTracks.add(new Track("The Beatles", title));
		}
		assertTracks(tracks, expectedTracks);
	}

	@Test
	public void findsVariousArtistsTracks() throws Exception {
		scannerService.add(set(media5));
		
		Artist artist = new Artist("Various Artists");
		int artistId = musicDao.getArtistId(artist);
		
		List<Album> albums = browserService.getAlbums(artistId, true);
		assertAlbums(albums, artist, "Music From Searching For Wrong-Eyed Jesus");
		
		Album album = albums.get(0);
		List<Track> tracks = browserService.getTracks(album.getTrackIds());
		List<Track> expectedTracks = asList(new Track("Harry Crews", "Everything Was Stories"),
				new Track("Jim White", "Still Waters"),
				new Track("The Handsome Family", "My Sister's Tiny Hands"));
		assertTracks(tracks, expectedTracks);
	}

	@Test
	public void findsComposerTag() throws Exception {
		scannerService.add(set(media6));
		
		List<Track> tracks = browserService.getTracks(Arrays.asList(
				browserService.getTrackId(media6 + separatorChar + "composer.mp3")));
		
		Assert.assertNotNull(tracks);
		Assert.assertEquals(1, tracks.size());
		Assert.assertEquals("Composer", tracks.get(0).getMetaData().getComposer());
	}
	
	@Test
	public void handlesEmptyMediaFolder() throws Exception {
		scannerService.add(set(media4));
		scannerService.delete(set(media4));
	}
	
	@Test
	public void handlesMultipleImportOfSameFolder() throws Exception {
		// aretha is below media2 and gets scanned twice
		scannerService.add(set(media2, aretha));

		assertArtists(browserService.getArtists(), "The Beatles", "Aretha Franklin");
	}
	
	private void assertArtists(List<Artist> artists, String... artistNames) {
		Assert.assertNotNull(artists);
		Assert.assertEquals(artistNames.length, artists.size());
		for (String artistName : artistNames) {
			Assert.assertTrue(artists.contains(new Artist(artistName)));
		}
	}

	private void assertAlbums(List<Album> albums, Artist artist, String... albumNames) {
		Assert.assertNotNull(albums);
		Assert.assertEquals(albumNames.length, albums.size());
		for (Album album : albums) {
			album.setArtist(artist); // not returned by db
		}
		for (String albumName : albumNames) {
			Assert.assertTrue(albums.contains(new Album(artist, albumName)));
		}
	}
	
	private void assertTracks(List<Track> tracks, List<Track> expectedTracks) {
		Assert.assertNotNull(tracks);
		Assert.assertEquals(tracks.size(), expectedTracks.size());

		Comparator<Track> trackComparator = new Comparator<Track>() {
			@Override
			public int compare(Track t1, Track t2) {
				return t1.getName().compareTo(t2.getName());
			}
		};
		Collections.sort(tracks, trackComparator);
		Collections.sort(expectedTracks, trackComparator);
		
		for (int i = 0; i < tracks.size(); i++) {
			Assert.assertEquals(tracks.get(i).getName(), expectedTracks.get(i).getName());
			Assert.assertEquals(tracks.get(i).getArtist().getName(), 
					expectedTracks.get(i).getArtist().getName());
		}
	}
	
	private Album getAlbum(List<Album> albums, String albumName) {
		for (Album album : albums) {
			if (albumName.equals(album.getName())) {
				return album;
			}
		}
		Assert.fail();
		return null;
	}

}