package com.github.hakko.musiccabinet.dao.jdbc;

import static com.github.hakko.musiccabinet.dao.util.PostgreSQLFunction.UPDATE_MB_ALBUM;
import static junit.framework.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.hakko.musiccabinet.dao.util.PostgreSQLUtil;
import com.github.hakko.musiccabinet.domain.model.music.Artist;
import com.github.hakko.musiccabinet.domain.model.music.MBAlbum;
import com.github.hakko.musiccabinet.exception.ApplicationException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:applicationContext.xml"})
public class JdbcMusicBrainzAlbumDaoTest {
	
	@Autowired
	private JdbcMusicDao musicDao;

	@Autowired
	private JdbcMusicBrainzAlbumDao albumDao;
	
	private static final String ARTIST = "Cult of Luna",
	TITLE1 = "The Beyond", MBID1 = "236316f7-c919-3986-918b-25e135ba8000", TYPE1 = "Album",
	TITLE2 = "Bodies / Recluse", MBID2 = "5484925b-884c-31d8-9c3e-2ef3824e6a5f", TYPE2 = "EP";
	private static final short YEAR1 = 2003, YEAR2 = 2006;
	
	@Before
	public void prepareTestData() throws ApplicationException {
		PostgreSQLUtil.loadFunction(albumDao, UPDATE_MB_ALBUM);
	}

	@Test
	public void createsAlbums() {
		Artist artist = new Artist(ARTIST);
		musicDao.setArtistId(artist);
		MBAlbum album1 = new MBAlbum(TITLE1, MBID1, YEAR1, TYPE1);
		MBAlbum album2 = new MBAlbum(TITLE2, MBID2, YEAR2, TYPE2);
		album1.setArtist(artist);
		album2.setArtist(artist);
		albumDao.createAlbums(Arrays.asList(album1, album2));
		
		List<MBAlbum> albums = albumDao.getAlbums(artist.getId());
		assertEquals(2, albums.size());
		assertEquals(album1.getTitle(), albums.get(0).getTitle());
		assertEquals(album2.getTitle(), albums.get(1).getTitle());
	}

}